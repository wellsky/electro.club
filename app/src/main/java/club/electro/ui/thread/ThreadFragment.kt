package club.electro.ui.thread

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import club.electro.adapter.PostAdapter
import club.electro.adapter.PostInteractionListener
import club.electro.databinding.FragmentThreadBinding
import club.electro.utils.LongArg
import club.electro.utils.ByteArg
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import club.electro.MainViewModel
import club.electro.R
import club.electro.ui.user.UserProfileFragment.Companion.userId
import club.electro.util.AndroidUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import androidx.core.view.isVisible
import club.electro.ToolBarConfig
import club.electro.di.DependencyContainer
import club.electro.dto.*
import club.electro.repository.ThreadLoadTarget
import club.electro.repository.ThreadLoadTarget.Companion.TARGET_POSITION_FIRST
import club.electro.repository.ThreadLoadTarget.Companion.TARGET_POSITION_FIRST_UNREAD
import club.electro.repository.ThreadLoadTarget.Companion.TARGET_POSITION_LAST
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoId
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoType
import club.electro.utils.HtmlToText
import club.electro.utils.UrlHandler


class ThreadFragment : Fragment() {
    companion object {
        var Bundle.threadType: Byte by ByteArg
        var Bundle.threadId: Long by LongArg
        var Bundle.postId: Long by LongArg // Может быть -1 (загрузить с последнего сообщения) и -2 (с первого непрочитанного)
    }

    private var threadType: Byte = 0
    private var threadId: Long = 0

    private lateinit var viewModel: ThreadViewModel
    private var _binding: FragmentThreadBinding? = null

    private val binding get() = _binding!!

    private var currentTargetPost: ThreadLoadTarget? = null // Задается при вызове загрузки постов и сбрасывается в null при первом поступлении новых данных
    private var currentIncomingRefresh: Boolean = false // Становится true, если тема обновляется из-за изменений на сервере

    private var scrolledToTop: Boolean = true
    private var scrolledToBottom: Boolean = true

    private val appAuth = DependencyContainer.getInstance().appAuth

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            viewModel.thread.observe(viewLifecycleOwner) {
                it?.let {
                    mainViewModel.updateActionBarTitle(ToolBarConfig(
                        title1 = it.name,
                        title2 = getString(R.string.subscribers_count) + ": " + it.subscribersCount.toString(),
                        onClick = {
                            findNavController().navigate(
                                R.id.action_threadFragment_to_threadInfoFragment,
                                Bundle().apply {
                                    threadInfoType = it.type
                                    threadInfoId = it.id
                                }
                            )
                        }
                    ))
                }
            }
        } ?: throw Throwable("Invalid activity")
    }

    // https://www.vogella.com/tutorials/AndroidActionBar/article.html
    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        viewModel.thread.value?.let { thread ->
            activity?.run {
                if (thread.type == THREAD_TYPE_PUBLIC_CHAT) {
                    menuInflater.inflate(R.menu.menu_thread, menu)
                    if (thread.subscriptionStatus.equals(SUBSCRIPTION_STATUS_NONE)) {
                        menu.findItem(R.id.thread_unsubscribe).isVisible = false
                        menu.findItem(R.id.thread_mute).isVisible = false
                    } else {
                        menu.findItem(R.id.thread_subscribe).isVisible = false
                    }
                }
            }
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.thread_settings -> {
                findNavController().navigate(
                    R.id.action_threadFragment_to_threadInfoFragment,
                    Bundle().apply {
                        threadInfoType = this@ThreadFragment.threadType
                        threadInfoId = this@ThreadFragment.threadId
                    }
                )
                return true
            }
            R.id.thread_subscribe -> {
                viewModel.changeSubscription(SUBSCRIPTION_STATUS_SUBSCRIBED)
                return true
            }
            R.id.thread_unsubscribe -> {
                viewModel.changeSubscription(SUBSCRIPTION_STATUS_NONE)
                return true
            }
            R.id.thread_mute -> {
                viewModel.changeSubscription(SUBSCRIPTION_STATUS_MUTED)
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        threadType = requireArguments().threadType
        threadId =  requireArguments().threadId

        val postId = requireArguments().postId

//        currentTargetPost = when (postId) {
//             0L -> ThreadLoadTarget(ThreadLoadTarget.TARGET_POSITION_LAST)
//            -1L -> ThreadLoadTarget(ThreadLoadTarget.TARGET_POSITION_FIRST)
//            -2L -> ThreadLoadTarget(ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD)
//            else -> ThreadLoadTarget(postId)
//        }

        currentTargetPost = ThreadLoadTarget(postId)

        viewModel = ViewModelProvider(this, ThreadViewModelFactory(
            requireActivity().getApplication(),
            threadType,
            threadId,
            currentTargetPost
        )).get(ThreadViewModel::class.java)

        viewModel.getThread()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var firstUpdateTimeReceived = false

        _binding = FragmentThreadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = PostAdapter(object : PostInteractionListener {
            override fun onEditClicked(post: Post) {
                viewModel.startEditPost(post)
            }

            override fun onAnswerClicked(post: Post) {
                viewModel.startAnswerPost(post)
            }

            override fun onRemoveClicked(post: Post) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(getString(R.string.delete_post_confirm_text))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.delete_post_confirm_yes)) { dialog, id ->
                        viewModel.removePost(post)
                    }
                    .setNegativeButton(getString(R.string.delete_post_confirm_no)) { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }

            override fun onAvatarClicked(post: Post) {
                findNavController().navigate(
                    R.id.action_threadFragment_to_userProfileFragment,
                    Bundle().apply {
                        userId = post.authorId
                    }
                )
            }

            override fun onUrlClicked(url: String?) {
                UrlHandler(requireContext(), findNavController()).setUrl(url).open()
            }
        })
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.postsList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            println("LaunchWhenCreated")
            viewModel.posts.collectLatest {
                currentTargetPost?.let {
                    setGravityForTarget(it)
                    currentTargetPost = null
                }

                adapter.submitData(it)
            }

            adapter.loadStateFlow.collectLatest { state ->
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading
            }
        }

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            // https://stackoverflow.com/questions/51889154/recycler-view-not-scrolling-to-the-top-after-adding-new-item-at-the-top-as-chan
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (currentIncomingRefresh) {
                    if (binding.postsList.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (scrolledToBottom) {
                            binding.postsList.smoothScrollToPosition(0);
                        } else {
                            //TODO Внизу появились новые сообщения
                            println("new messages in bottom of the list")
                        }
                    }
                    currentIncomingRefresh = false
                }
            }
        })

        // Обновляет видимые посты, если увеличилось время последнего изменения на сервере подписок
        viewModel.threadStatus.observe(viewLifecycleOwner) {
            if (it.lastUpdateTime > 0L) {
                if (firstUpdateTimeReceived) {
                    adapter.refresh()
                    currentIncomingRefresh = true
                    // TODO надо убрать currentIncomingRefresh и ввести newPostReceived
                } else {
                    firstUpdateTimeReceived = true
                }
            }
        }


        viewModel.editedPost.observe(viewLifecycleOwner) {
            if (it.id != 0L) {
                binding.editedPostGroup.visibility = View.VISIBLE
                binding.editedPostContent.text = HtmlToText(it.content)

                with (binding.editorPostContent) {
                    val editorText = it.content
                    setText(editorText)
                    requestFocus()
                }
            } else {
                binding.editedPostGroup.visibility = View.GONE
                binding.editorPostContent.setText("")
            }
        }

        viewModel.answerToPost.observe(viewLifecycleOwner) {
            if (it.id != 0L) {
                binding.answerPostGroup.visibility = View.VISIBLE
                binding.answerToContent.text = HtmlToText(it.content)
            } else {
                binding.answerPostGroup.visibility = View.GONE
            }
        }

        appAuth.authState.observe(viewLifecycleOwner) {
            requireActivity().invalidateOptionsMenu()
            invalidateBottomPanel()
        }

        viewModel.thread.observe(viewLifecycleOwner) {
            requireActivity().invalidateOptionsMenu()
            invalidateBottomPanel()
        }

        binding.postsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // Scrolling down
                    binding.buttonScrollToBegin.isVisible = false
                    if (!scrolledToBottom) {
                        binding.buttonScrollToEnd.isVisible = true
                    }
                } else {
                    // Scrolling up
                    if (!scrolledToTop) {
                        binding.buttonScrollToBegin.isVisible = true
                    }
                    binding.buttonScrollToEnd.isVisible = false
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    // Вверху больше ничего нет
                    scrolledToTop = true
                    binding.buttonScrollToBegin.isVisible = false
                } else {
                    scrolledToTop = false
                }

                if (!recyclerView.canScrollVertically(1)) {
                    // Внизу больше ничего нет
                    scrolledToBottom = true
                    binding.buttonScrollToEnd.isVisible = false
                } else {
                    scrolledToBottom = false
                }
            }
        })

        binding.testButton.setOnClickListener {
            adapter.refresh()
        }

        binding.buttonScrollToBegin.setOnClickListener {
            currentTargetPost = ThreadLoadTarget(TARGET_POSITION_FIRST)
            viewModel.reloadPosts(currentTargetPost!!)

        }

        binding.buttonScrollToEnd.setOnClickListener {
            currentTargetPost = ThreadLoadTarget(TARGET_POSITION_LAST)
            viewModel.reloadPosts(currentTargetPost!!)
        }

        binding.editorPostSave.setOnClickListener {
            with (binding.editorPostContent) {
                if (text.isNullOrBlank()) {
                    Snackbar.make(binding.root, R.string.error_empty_post, Snackbar.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }
                viewModel.changeEditorPostContent(text.toString())
                viewModel.saveEditorPost()

                setText("")
                clearFocus()
                AndroidUtils.hideKeyboard(it)
                currentTargetPost = ThreadLoadTarget(TARGET_POSITION_LAST)
            }
        }

        binding.cancelEdit.setOnClickListener {
            viewModel.cancelEditPost()
            with (binding.editorPostContent) {
                setText("")
                clearFocus()
                AndroidUtils.hideKeyboard(it)
            }
        }

        binding.cancelAnswer.setOnClickListener {
            viewModel.cancelAnswerPost()
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        viewModel.startCheckUpdates()

        setHasOptionsMenu(true)

        return root
    }

    fun invalidateBottomPanel() {
        var showBottomPanel = false
        viewModel.thread.value?.let {
            if (it.subscriptionStatus.equals(SUBSCRIPTION_STATUS_SUBSCRIBED)) {
                if (appAuth.authorized()) {
                    showBottomPanel = true
                }
            }
        }
        binding.bottomPanel.isVisible = showBottomPanel
    }

    /**
     * Необходимо остановить корутину в репозитории, которая опрашивает сервер об обновлениях в текущем thread
     */
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopCheckUpdates()
    }


    /**
     * Прижимает все сообщения к верху
     * Гравитация была добавлена из-за асинхронной подгрузки изображений в сообщения
     * Если при нижней гравитации перемотать чат до самого верха, то загрузится первое сообщение, сфокусируется,
     * а потом начнет уползать вверх из-за подгружаемых ниже изображений, вместо того чтобы нижние сообщения ползли вниз.
     * И дойдет даже до того, что начнут грузится все новые и новые страницы.
     */
    fun setGravityTop() {
        val linearLayoutManager = LinearLayoutManager(this.context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.postsList.setLayoutManager(linearLayoutManager)
    }

    /**
     * Прижимает все сообщения к низу
     */
    fun setGravityBottom() {
        val linearLayoutManager = LinearLayoutManager(this.context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = false
        binding.postsList.setLayoutManager(linearLayoutManager)
    }

    /**
     * Устанавливает гравитацию в зависимости от целевого поста
     */
    fun setGravityForTarget(target: ThreadLoadTarget) {
        if (target.targetPostId != null) {
            setGravityTop()
        }
        if (target.targetPostId == TARGET_POSITION_FIRST_UNREAD) {
            setGravityTop()
            scrolledToTop = true
            binding.buttonScrollToBegin.isVisible = false

        }
        if (target.targetPostId == TARGET_POSITION_FIRST) {
            setGravityTop()
            scrolledToTop = true
            binding.buttonScrollToBegin.isVisible = false

        }
        if (target.targetPostId == TARGET_POSITION_LAST) {
            setGravityBottom()
            scrolledToBottom = true
            binding.buttonScrollToEnd.isVisible = false
        }
    }

}
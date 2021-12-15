package club.electro.ui.thread

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import club.electro.adapter.PostAdapter
import club.electro.adapter.PostInteractionListener
import club.electro.databinding.FragmentThreadBinding
import club.electro.dto.Post
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
import club.electro.repository.ThreadLoadTarget
import club.electro.ui.subscriptions.SubscriptionsViewModel
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoId
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoType
import club.electro.utils.StripTags
import club.electro.utils.UrlHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ThreadFragment : Fragment() {
    companion object {
        var Bundle.threadType: Byte by ByteArg
        var Bundle.threadId: Long by LongArg
        var Bundle.postId: Long by LongArg // Может быть -1 (загрузить с последнего сообщения) и -2 (с первого непрочитанного)
    }

    private lateinit var viewModel: ThreadViewModel
    private var _binding: FragmentThreadBinding? = null

    private val binding get() = _binding!!

    private var currentTargetPost: ThreadLoadTarget? = null // Задается при вызове загрузки постов и сбрасывается в null при первом поступлении новых данных

    private var firstUpdateTimeReceived = false

    private var scrolledToTop: Boolean = true
    private var scrolledToBottom: Boolean = true


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val threadType = requireArguments().threadType
        val threadId =  requireArguments().threadId
        val postId = requireArguments().postId

        // TODO как сделать, чтобы передаваемые аргументы во фрагмент могли быть nullable. Сейчас если LongArg не передан, то возвращает 0
        currentTargetPost = when (postId) {
             0L -> ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_LAST)
            -1L -> ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_LAST)
            -2L -> ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD)
            else -> ThreadLoadTarget(targetPostId = postId)
        }

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
                builder.setMessage("Are you sure you want to Delete?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        viewModel.removePost(post)
                    }
                    .setNegativeButton("No") { dialog, id ->
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

        binding.postsList.adapter = adapter


        lifecycleScope.launchWhenCreated {
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
//                    state.prepend is LoadState.Loading ||
//                    state.append is LoadState.Loading
            }
        }

        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            // https://stackoverflow.com/questions/51889154/recycler-view-not-scrolling-to-the-top-after-adding-new-item-at-the-top-as-chan
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // TODO нужен нормальный метод для определения публикации новых постов
                val newPostsPublished = false //((positionStart == 0) and (itemCount == 1))

                if (newPostsPublished) {
                    if (binding.postsList.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (scrolledToBottom) {
                            binding.postsList.smoothScrollToPosition(0);
                        } else {
                            //TODO Внизу появились новые сообщения
                            println("new messages in bottom of the list")
                        }
                    }
                }
            }
        })

//        lifecycleScope.launchWhenCreated {
//            adapter.loadStateFlow.collectLatest { state ->
//                binding.swiperefresh.isRefreshing =
//                    state.refresh is LoadState.Loading
////                    state.prepend is LoadState.Loading ||
////                    state.append is LoadState.Loading
//            }
//        }

        // TODO реализовано криво. Вообще логику обновления лучше не выносить за репозиторий.
        // Но только adapter.refresh() может указать медиатору на текущий видимый пост
        viewModel.lastUpdateTime.observe(viewLifecycleOwner) {
            if (it > 0L) {
                if (firstUpdateTimeReceived) {
                    adapter.refresh()
                } else {
                    firstUpdateTimeReceived = true
                }
            }
        }


        viewModel.editedPost.observe(viewLifecycleOwner) {
            if (it.id != 0L) {
                binding.editedPostGroup.visibility = View.VISIBLE
                binding.editedPostContent.text = StripTags(it.content).toString()

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
                binding.answerToContent.text = StripTags(it.content).toString()
            } else {
                binding.answerPostGroup.visibility = View.GONE
            }
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
            currentTargetPost = ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_FIRST)
            viewModel.reloadPosts(currentTargetPost!!)

        }

        binding.buttonScrollToEnd.setOnClickListener {
            currentTargetPost = ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_LAST)
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
                currentTargetPost = ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_LAST)
            }
            //binding.editMessageGroup.visibility = View.GONE
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

        return root
    }

    // TODO не работает. Надо сделать сохранение текущей позиции.
    // https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state/61609823#61609823

//    override fun onPause() {
//        super.onPause()
//        lastFirstVisiblePosition = (binding.postsList.getLayoutManager() as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        (binding.postsList.getLayoutManager() as LinearLayoutManager).scrollToPosition(lastFirstVisiblePosition)
//    }


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
            println("gravity1")
            setGravityTop()
            //binding.postsList.scrollToPosition((binding.postsList.getAdapter()!!.getItemCount()))
        }
        if (target.targetPostPosition == ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD) {
            println("gravity2")
            setGravityTop()
            //binding.postsList.smoothScrollToPosition((binding.postsList.getAdapter()!!.getItemCount() - 1))
            scrolledToTop = true
            binding.buttonScrollToBegin.isVisible = false

        }
        if (target.targetPostPosition == ThreadLoadTarget.TARGET_POSITION_FIRST) {
            println("gravity3")
            setGravityTop()
            //binding.postsList.smoothScrollToPosition((binding.postsList.getAdapter()!!.getItemCount() - 1))
            scrolledToTop = true
            binding.buttonScrollToBegin.isVisible = false

        }
        if (target.targetPostPosition == ThreadLoadTarget.TARGET_POSITION_LAST) {
            println("gravity4")
            setGravityBottom()
            //binding.postsList.smoothScrollToPosition(0)
            scrolledToBottom = true
            binding.buttonScrollToEnd.isVisible = false
        }
    }

}

//      Перехват изменения состояния скроллинга
//        binding.postsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState === RecyclerView.SCROLL_STATE_IDLE) {
//                    println(getCurrentItem())
//                }
//            }
//        })
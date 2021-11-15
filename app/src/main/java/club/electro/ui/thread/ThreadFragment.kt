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
import club.electro.adapter.PostTextPreparator
import club.electro.ui.user.UserProfileFragment.Companion.userId
import club.electro.util.AndroidUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import androidx.core.view.isVisible
import club.electro.ToolBarConfig
import club.electro.repository.ThreadTargetPost
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoId
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoType


class ThreadFragment : Fragment() {
    companion object {
        var Bundle.threadType: Byte by ByteArg
        var Bundle.threadId: Long by LongArg
    }

    private lateinit var viewModel: ThreadViewModel
    private var _binding: FragmentThreadBinding? = null

    private val binding get() = _binding!!

    private var currentTargetPost: ThreadTargetPost? = null

    private var firstUpdateTimeReceived = false

    private var lastFirstVisiblePosition = 0
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val threadType = requireArguments().threadType
        val threadId =  requireArguments().threadId


        // TODO явно есть более красивый способ
//        threadName?.let {
//            (activity as AppCompatActivity?)!!.supportActionBar!!.title = it
//        }


        viewModel = ThreadViewModel(
            requireActivity().getApplication(),
            threadType!!,
            threadId!!
        )

        viewModel.getThread()
//        viewModel.loadPosts()

        _binding = FragmentThreadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val context1 = requireContext()

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
        })

        binding.postsList.adapter = adapter

        // TODO после перехода на Pager блок внизу не работает. В адаптере больше нет текущего списка
//        viewModel.data.observe(viewLifecycleOwner, { items ->
//            val newPostPublished: Boolean = if (!adapter.currentList.isEmpty() && !items.isEmpty()) {
//                val oldLastPost: Post = adapter.currentList.first()
//                val newLastPost: Post = items.first()
//                (newLastPost.published > oldLastPost.published)
//            } else {
//                !items.isEmpty()
//            }
//
//            adapter.submitList(items)
//
//            if (newPostPublished) {
//                if (binding.postsList.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
//                    if (getFocusedItem() == 0) {
//                        binding.postsList.smoothScrollToPosition(0);
//                    } else {
//                        //TODO Внизу появились новые сообщения
//                    }
//                }
//            }
//        })

//        viewModel.thread.observe(viewLifecycleOwner) {
//            activity?.run {
//                val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
//                it?.let {
//                    mainViewModel.updateActionBarTitle(ToolBarConfig(
//                        title1 = it.name,
//                        title2 = getString(R.string.subscribers_count) + ": " + it.subscribersCount.toString(),
//                        onClick = {
//                            findNavController().navigate(
//                                R.id.action_threadFragment_to_threadInfoFragment,
//                                Bundle().apply {
//                                    threadInfoType = it.type
//                                    threadInfoId = it.id
//                                }
//                            )
//                        }
//                    ))
//                }
//            }
//        }

        lifecycleScope.launchWhenCreated {
            viewModel.posts.collectLatest {
                adapter.submitData(it)
            }
        }

        // https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state/61609823#61609823
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            // https://stackoverflow.com/questions/51889154/recycler-view-not-scrolling-to-the-top-after-adding-new-item-at-the-top-as-chan
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                currentTargetPost?.let {
                    if (it.targetPostPosition == ThreadTargetPost.TARGET_POSITION_FIRST) {
                        setGravityTop()
                        binding.postsList.scrollToPosition((binding.postsList.getAdapter()!!.getItemCount() - 1))
                        scrolledToTop = true
                        binding.buttonScrollToBegin.isVisible = false

                    }
                    if (it.targetPostPosition == ThreadTargetPost.TARGET_POSITION_LAST) {
                        setGravityBottom()
                        binding.postsList.smoothScrollToPosition(0)
                        scrolledToBottom = true
                        binding.buttonScrollToEnd.isVisible = false
                    }
                }

                currentTargetPost = null

                // TODO нужен нормальный метод для определения публикации новых постов
                val newPostsPublished = ((positionStart == 0) and (itemCount == 1))

                if (newPostsPublished) {
                    if (binding.postsList.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (getFocusedItem() == 0) {
                            binding.postsList.smoothScrollToPosition(0);
                        } else {
                            //TODO Внизу появились новые сообщения
                            println("new messages in bottom of the list")
                        }
                    }
                }


            }
        })

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                    state.prepend is LoadState.Loading ||
                    state.append is LoadState.Loading
            }
        }

        // TODO реализовано криво. Вообще логику обновления лучше не выносить за репозиторий.
        // Но почему-то вызванная из checkForUpdates() функция reloadPosts() не релодит посты
        viewModel.lastUpdateTime.observe(viewLifecycleOwner) {
            if (it > 0L) {
                if (firstUpdateTimeReceived) {
                    adapter.refresh()
                    //viewModel.reloadPosts()
                } else {
                    firstUpdateTimeReceived = true
                }
            }
        }


        viewModel.editedPost.observe(viewLifecycleOwner) {
            if (it.id != 0L) {
                binding.editedPostGroup.visibility = View.VISIBLE
                binding.editedPostContent.text = it.content

                with (binding.editorPostContent) {
                    requestFocus()
                    //setText(HtmlCompat.fromHtml(it.content, HtmlCompat.FROM_HTML_MODE_LEGACY))
                    val editorText = PostTextPreparator(it.content)
                        .prepareBasicTags()
                        .prepareEmojies()
                        .preparePlainText()
                        .get()
                    setText(editorText)
                }
            } else {
                binding.editedPostGroup.visibility = View.GONE
                binding.editorPostContent.setText("")
            }
        }

        viewModel.answerToPost.observe(viewLifecycleOwner) {
            if (it.id != 0L) {
                binding.answerPostGroup.visibility = View.VISIBLE
                binding.answerToContent.text = it.content
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
                    // Внизу больше ничего нет
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
            println(binding.postsList.getAdapter()!!.getItemCount())
            binding.postsList.smoothScrollToPosition((binding.postsList.getAdapter()!!.getItemCount() - 1))

            setGravityTop()
        }

        binding.buttonScrollToBegin.setOnClickListener {
            viewModel.loadThreadBegining()
            adapter.refresh()
            currentTargetPost = ThreadTargetPost(targetPostPosition = ThreadTargetPost.TARGET_POSITION_FIRST)
        }

        binding.buttonScrollToEnd.setOnClickListener {
            viewModel.loadThreadEnd()
            adapter.refresh()
            currentTargetPost = ThreadTargetPost(targetPostPosition = ThreadTargetPost.TARGET_POSITION_LAST)
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
                currentTargetPost = ThreadTargetPost(targetPostPosition = ThreadTargetPost.TARGET_POSITION_LAST)
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

        // TODO добавить swipeToRefresh перетягиванием вниз и вызовом adapter.refresh()
        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        setGravityBottom()

        return root
    }

    /**
     * Возвращает порядковый номер поста, на котором находится фокус
     */
    fun getFocusedItem(): Int {
        return (binding.postsList.getLayoutManager() as LinearLayoutManager)
        .findFirstVisibleItemPosition()
    }

    // TODO не работает. Надо сделать сохранение текущей позиции.
    // https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state/61609823#61609823
    override fun onPause() {
        super.onPause()
        lastFirstVisiblePosition = (binding.postsList.getLayoutManager() as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
    }

    override fun onResume() {
        super.onResume()
        (binding.postsList.getLayoutManager() as LinearLayoutManager).scrollToPosition(lastFirstVisiblePosition)
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
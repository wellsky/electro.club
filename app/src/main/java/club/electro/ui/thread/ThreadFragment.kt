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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import club.electro.util.StringArg
import androidx.recyclerview.widget.LinearLayoutManager
import club.electro.MainViewModel
import club.electro.R
import club.electro.adapter.PostTextPreparator
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadName
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.ui.user.UserProfileFragment.Companion.userId
import club.electro.util.AndroidUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest

class ThreadFragment : Fragment() {
    companion object {
        var Bundle.threadType: Byte by ByteArg
        var Bundle.threadId: Long by LongArg
        var Bundle.threadName: String? by StringArg
    }

    private lateinit var viewModel: ThreadViewModel
    private var _binding: FragmentThreadBinding? = null

    private val binding get() = _binding!!

    private var threadName: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarTitle(threadName?: "Unknown thread")
        } ?: throw Throwable("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val threadType = arguments?.threadType
        val threadId = arguments?.threadId
        threadName = arguments?.threadName

        // TODO явно есть более красивый способ
//        threadName?.let {
//            (activity as AppCompatActivity?)!!.supportActionBar!!.title = it
//        }


        viewModel = ThreadViewModel(
            requireActivity().getApplication(),
            threadType!!,
            threadId!!
        )

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
                    // Delete selected note from database
                        //var dbManager = DbManager(this.context!!)
                        //val selectionArgs = arrayOf(myNote.nodeID.toString())
                        //dbManager.delete("ID=?", selectionArgs)
                        //LoadQuery("%")
                    }
                    .setNegativeButton("No") { dialog, id ->
                        // Dismiss the dialog
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

        lifecycleScope.launchWhenCreated {
            //viewModel.data.collectLatest(adapter::submitData)
            // То же самое что и:
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                    state.prepend is LoadState.Loading ||
                    state.append is LoadState.Loading
            }
        }


//        viewModel.editorPost.observe(viewLifecycleOwner) {
//            with (binding.editorPostContent) {
//                requestFocus()
//                //setText(HtmlCompat.fromHtml(it.content, HtmlCompat.FROM_HTML_MODE_LEGACY))
//                val editorText = PostTextPreparator(it.content)
//                    .prepareBasicTags()
//                    .prepareEmojies()
//                    .preparePlainText()
//                    .get()
//                setText(editorText)
//            }
//        }

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

        return root
    }

    /**
     * Возвращает порядковый номер поста, на котором находится фокус
     */
    fun getFocusedItem(): Int {
        return (binding.postsList.getLayoutManager() as LinearLayoutManager)
        .findFirstVisibleItemPosition()
    }

    /**
     * Необходимо остановить корутину в репозитории, которая опрашивает сервер об обновлениях в текущем thread
     */
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stop()
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
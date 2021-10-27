package club.electro.ui.thread

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import club.electro.adapter.PostAdapter
import club.electro.adapter.PostInteractionListener
import club.electro.databinding.FragmentThreadBinding
import club.electro.dto.Post
import club.electro.utils.LongArg
import club.electro.utils.ByteArg
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import club.electro.util.StringArg
import androidx.recyclerview.widget.LinearLayoutManager
import club.electro.MainViewModel
import club.electro.R
import club.electro.util.AndroidUtils
import com.google.android.material.snackbar.Snackbar

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
        } ?: throw Throwable("invalid activity")
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

        viewModel.loadPosts()

        _binding = FragmentThreadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = PostAdapter(object : PostInteractionListener {
            override fun onClick(post: Post) {

            }
        })

        binding.postsList.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner, { items ->
            val newPostPublished: Boolean = if (!adapter.currentList.isEmpty() && !items.isEmpty()) {
                val oldLastPost: Post = adapter.currentList.first()
                val newLastPost: Post = items.first()
                (newLastPost.published > oldLastPost.published)
            } else {
                !items.isEmpty()
            }

            adapter.submitList(items)

            if (newPostPublished) {
                if (binding.postsList.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (getFocusedItem() == 0) {
                        binding.postsList.smoothScrollToPosition(0);
                    } else {
                        //TODO Внизу появились новые сообщения
                    }
                }
            }
        })


        viewModel.editorPost.observe(viewLifecycleOwner) {
            /*
            if (it.id != 0L) {
                with (binding.content) {
                    requestFocus()
                    setText(it.content)
                }
                binding.editMessageGroup.visibility = View.VISIBLE
                binding.editMessageContent.text = it.content
            }
            */
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

//        binding.cancelEdition.setOnClickListener {
//            with(binding.content) {
//                viewModel.cancelEdit()
//                setText("")
//                clearFocus()
//                AndroidUtils.hideKeyboard(it)
//            }
//            binding.editMessageGroup.visibility = View.GONE
//        }
//
//        binding.postEditorButton.setOnClickListener {
//            editPostLauncher.launch(null)
//        }


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
    override fun onDestroy() {
        super.onDestroy()
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
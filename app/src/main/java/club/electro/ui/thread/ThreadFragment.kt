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
import androidx.recyclerview.widget.RecyclerView
import club.electro.util.StringArg
import androidx.recyclerview.widget.LinearLayoutManager

class ThreadFragment : Fragment() {
    companion object {
        var Bundle.threadType: Byte by ByteArg
        var Bundle.threadId: Long by LongArg
        var Bundle.threadName: String? by StringArg
    }

    private lateinit var viewModel: ThreadViewModel
    private var _binding: FragmentThreadBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val threadType = arguments?.threadType
        val threadId = arguments?.threadId
        val threadName = arguments?.threadName

        // TODO явно есть более красивый способ
        threadName?.let {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = it
        }


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
            adapter.submitList(items)
            if (binding.postsList.scrollState === RecyclerView.SCROLL_STATE_IDLE) {
                if (getCurrentItem() == 0) {
                    binding.postsList.smoothScrollToPosition(0);
                } else {
                    //TODO Внизу появились новые сообщения
                }
            }
        })

        return root
    }

    /**
     * Возвращает порядковый номер поста, на котором находится фокус
     */
    fun getCurrentItem(): Int {
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
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
import androidx.appcompat.app.AppCompatActivity
import club.electro.util.StringArg


class ThreadFragment : Fragment() {
    companion object {
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
        val threadId = arguments?.threadId
        val threadName = arguments?.threadName

        // TODO явно есть более красивый способ
        threadName?.let {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = it
        }

        threadId?.let {
            viewModel = ThreadViewModel(
                requireActivity().getApplication(),
                threadId
            )
        }


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
        })

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(ThreadViewModel::class.java)
    }

}
package club.electro.ui.thread

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.adapter.PostAdapter
import club.electro.adapter.PostInteractionListener
import club.electro.databinding.FragmentSubscriptionsBinding
import club.electro.databinding.FragmentThreadBinding
import club.electro.dto.Post
import club.electro.dto.SubscriptionArea
import club.electro.ui.subscriptions.SubscriptionsViewModel
import club.electro.utils.LongArg
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.util.StringArg


class ThreadFragment : Fragment() {
    companion object {
        var Bundle.threadId: Long by LongArg
        var Bundle.threadName: String? by StringArg
    }

    private lateinit var viewModel: ThreadViewModel
    private var _binding: FragmentThreadBinding? = null

//    private val viewModel: ThreadViewModel by viewModels (
//        ownerProducer = ::requireParentFragment
//    )

//    private val viewModel: ThreadViewModel by viewModels (
//        ownerProducer = ::requireParentFragment
//    )

//    private val viewModel: ThreadViewModel = ThreadViewModel(
//        application = Application(),
//        threadId =
//    )

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val threadId = arguments?.threadId
        val threadName = arguments?.threadName

        println("threadId: " + threadId)

        // TODO явно есть более красивый способ
        threadName?.let {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = it
        }

        threadId?.let {
            //viewModel = ViewModelProvider(this).get(ThreadViewModel::class.java)

            viewModel = ThreadViewModel(
                requireActivity().getApplication(),
                threadId
            )
        }


        viewModel.loadPosts()


        //viewModel = ViewModelProvider(this).get(ThreadViewModel::class.java)

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
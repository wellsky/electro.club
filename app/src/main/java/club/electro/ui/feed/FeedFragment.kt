package club.electro.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.adapter.*
import club.electro.databinding.FragmentFeedBinding
import club.electro.dto.FeedPost

class FeedFragment : Fragment() {

    private lateinit var feedViewModel: FeedViewModel
    private var _binding: FragmentFeedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

//    private val viewModel: FeedViewModel by viewModels (
//        ownerProducer = ::requireParentFragment
//    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        feedViewModel =
            ViewModelProvider(this).get(FeedViewModel::class.java)

        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = FeedPostAdapter(object : OnFeedPostInteractionListener {
            override fun onClick(feedPost: FeedPost) {
            }
        })

        binding.feedPostsList.adapter = adapter

        feedViewModel.data.observe(viewLifecycleOwner, { posts ->
            adapter.submitList(posts)
        })

//        val textView: TextView = binding.textHome
//        feedViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package club.electro.ui.feed

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.adapter.*
import club.electro.databinding.FragmentFeedBinding
import club.electro.dto.*
import club.electro.ui.thread.ThreadFragment.Companion.postId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val feedViewModel: FeedViewModel by viewModels ()

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarConfig(ToolBarConfig(
                title2 = "",
                onClick = {}
            ))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = FeedPostAdapter(object : OnFeedPostInteractionListener {
            override fun onClick(feedPost: FeedPost) {
                findNavController().navigate(
                    R.id.action_nav_feed_to_threadFragment,
                    Bundle().apply {
                        threadType = ThreadType.THREAD_TYPE_POST_WITH_COMMENTS.value
                        threadId = feedPost.id
                        postId = -1L
                    }
                )
            }
        })

        binding.feedPostsList.adapter = adapter

        feedViewModel.data.observe(viewLifecycleOwner, { posts ->
            adapter.submitList(posts)
            binding.swiperefresh.setRefreshing(false)
        })

        binding.swiperefresh.setOnRefreshListener {
            feedViewModel.getFeedPosts()
        }

        feedViewModel.getFeedPosts()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
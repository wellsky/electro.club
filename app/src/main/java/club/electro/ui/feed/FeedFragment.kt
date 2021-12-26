package club.electro.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.adapter.*
import club.electro.databinding.FragmentFeedBinding
import club.electro.dto.FeedPost
import club.electro.dto.THREAD_TYPE_POST_WITH_COMMENTS
import club.electro.ui.thread.ThreadFragment.Companion.postId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType

class FeedFragment : Fragment() {

    private lateinit var feedViewModel: FeedViewModel
    private var _binding: FragmentFeedBinding? = null

    private val binding get() = _binding!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarTitle(ToolBarConfig(title1 = getString(R.string.menu_feed)))
        } ?: throw Throwable("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        feedViewModel = ViewModelProvider(this).get(FeedViewModel::class.java)

        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = FeedPostAdapter(object : OnFeedPostInteractionListener {
            override fun onClick(feedPost: FeedPost) {
                findNavController().navigate(
                    R.id.action_nav_feed_to_threadFragment,
                    Bundle().apply {
                        threadType = THREAD_TYPE_POST_WITH_COMMENTS
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
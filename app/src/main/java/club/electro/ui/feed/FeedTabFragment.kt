package club.electro.ui.feed

import android.os.Bundle
import android.view.*
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.adapter.*
import club.electro.databinding.FragmentFeedTabBinding
import club.electro.dto.*
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedTabFragment : Fragment() {
    private val feedViewModel: FeedViewModel by viewModels ()

    private var _binding: FragmentFeedTabBinding? = null
    private val binding get() = _binding!!

    private var scrolledToTop: Boolean = true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarConfig(ToolBarConfig(
                subtitle = "",
                onClick = {}
            ))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedTabBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // TODO язык ленты постов
        println(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0))

        val adapter = FeedPostAdapter(object : OnFeedPostInteractionListener {
            override fun onClick(feedPost: FeedPost) {
                findNavController().navigate(
                    R.id.action_nav_feed_to_threadFragment,
                    Bundle().apply {
                        threadType = ThreadType.THREAD_TYPE_POST_WITH_COMMENTS.value
                        threadId = feedPost.id
                        targetPostId = -1L
                    }
                )
            }
        })

        binding.feedPostsList.adapter = adapter

        feedViewModel.data.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
            binding.swiperefresh.isRefreshing = false

            if (scrolledToTop) {
                binding.feedPostsList.smoothScrollToPosition(0);
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            feedViewModel.getFeedPosts()
        }

        binding.feedPostsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrolledToTop = !recyclerView.canScrollVertically(-1)
            }
        })

        feedViewModel.getFeedPosts()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
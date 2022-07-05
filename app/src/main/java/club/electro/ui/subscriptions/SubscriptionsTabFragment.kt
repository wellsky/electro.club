package club.electro.ui.subscriptions

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.adapter.SubscriptionAreaAdapter
import club.electro.adapter.SubscriptionAreaInteractionListener
import club.electro.databinding.FragmentSubscriptionsTabBinding
import club.electro.dto.SubscriptionArea
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionsTabFragment : Fragment() {
    private val viewModel: SubscriptionsViewModel by viewModels ()

    private var _binding: FragmentSubscriptionsTabBinding? = null
    private val binding get() = _binding!!

    private var currentGroup: Byte = 0

    private var scrolledToTop: Boolean = true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarConfig(ToolBarConfig(
                subtitle = "",
                scroll = true,
                onClick = {}
            ))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentGroup = requireArguments().get("group").toString().toByte() // TODO
        println("Subscriptions group : " + currentGroup )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubscriptionsTabBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = SubscriptionAreaAdapter(object : SubscriptionAreaInteractionListener {
            override fun onClick(area: SubscriptionArea) {
                findNavController().navigate(
                    R.id.action_nav_subscriptions_to_threadFragment,
                    Bundle().apply {
                        threadType = area.type
                        threadId = area.objectId
                        targetPostId = if (currentGroup == 0.toByte() || area.count == 0)
                                            ThreadLoadTarget.TARGET_POSITION_LAST
                                        else
                                            ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD
                    }
                )
            }
        })

        binding.subscriptionsList.adapter = adapter

        viewModel.items(currentGroup).observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.swiperefresh.isVisible = false
                if (viewModel.appAuth.authorized()) {
                    binding.emptyListHint.isVisible = true
                } else {
                    binding.notLoggedHint.isVisible = true
                }
            } else {
                binding.swiperefresh.isVisible = true
                binding.emptyListHint.isVisible = false
                binding.notLoggedHint.isVisible = false
            }

            adapter.submitList(items)
            binding.swiperefresh.isRefreshing = false

            if (scrolledToTop) {
                binding.subscriptionsList.smoothScrollToPosition(0);
            }
        }

        binding.subscriptionsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrolledToTop = !recyclerView.canScrollVertically(-1)
            }
        })

        binding.swiperefresh.setOnRefreshListener {
            viewModel.loadSubscriptions(currentGroup)
        }

        setHasOptionsMenu(true)

        return root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSubscriptions(currentGroup)
        viewModel.startCheckUpdates(currentGroup)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopCheckUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
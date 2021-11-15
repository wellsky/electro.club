package club.electro.ui.subscriptions

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
import club.electro.adapter.SubscriptionAreaAdapter
import club.electro.adapter.SubscriptionAreaInteractionListener
import club.electro.databinding.FragmentSubscriptionsBinding
import club.electro.dto.SubscriptionArea
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType

class SubscriptionsFragment : Fragment() {

    private lateinit var viewModel: SubscriptionsViewModel
    private var _binding: FragmentSubscriptionsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarTitle(ToolBarConfig(title1 = getString(R.string.menu_subscriptions)))
        } ?: throw Throwable("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(SubscriptionsViewModel::class.java)

        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = SubscriptionAreaAdapter(object : SubscriptionAreaInteractionListener {
            override fun onClick(area: SubscriptionArea) {
                findNavController().navigate(
                    R.id.action_nav_subscriptions_to_threadFragment,
                    Bundle().apply {
                        threadType = area.type
                        threadId = area.object_id
                    }
                )
            }
        })

        binding.subscriptionsList.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner, { items ->
            adapter.submitList(items)
        })

        binding.swiperefresh.setOnRefreshListener {
            binding.swiperefresh.setRefreshing(false)
            //binding.progress.isVisible = true
            //binding.newPostsButton.isVisible = false
            viewModel.loadPosts()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
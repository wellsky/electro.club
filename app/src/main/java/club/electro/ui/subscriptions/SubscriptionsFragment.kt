package club.electro.ui.subscriptions

import android.app.ActionBar
import android.os.Bundle
import android.view.*
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
import club.electro.repository.ThreadLoadTarget
import club.electro.ui.thread.ThreadFragment.Companion.postId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType

class SubscriptionsFragment : Fragment() {

    private lateinit var viewModel: SubscriptionsViewModel
    private var _binding: FragmentSubscriptionsBinding? = null

    private val binding get() = _binding!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarTitle(ToolBarConfig(title1 = getString(R.string.menu_subscriptions)))
        } ?: throw Throwable("Invalid activity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SubscriptionsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = SubscriptionAreaAdapter(object : SubscriptionAreaInteractionListener {
            override fun onClick(area: SubscriptionArea) {
                findNavController().navigate(
                    R.id.action_nav_subscriptions_to_threadFragment,
                    Bundle().apply {
                        threadType = area.type
                        threadId = area.object_id
                        postId = if (area.count > 0) ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD else ThreadLoadTarget.TARGET_POSITION_LAST // Если есть непрочитанные, то грузить с первого непрочитанного, иначе с последнего сообщения в теме
                    }
                )
            }
        })

        binding.subscriptionsList.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner, { items ->
            adapter.submitList(items)
            binding.swiperefresh.setRefreshing(false)
        })

        binding.swiperefresh.setOnRefreshListener {
            viewModel.loadSubscriptions()
        }

        viewModel.loadSubscriptions()

        viewModel.startCheckUpdates()

        setHasOptionsMenu(true)

        return root
    }

    override fun onDestroyView() {
        viewModel.stopCheckUpdates()
        super.onDestroyView()
        _binding = null
    }
}
package club.electro.ui.subscriptions

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

    private var globalList: Boolean = true

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        globalList = (requireArguments().get("global") == 1)
        println("Global : " + globalList )
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
                        targetPostId = if (area.count > 0) ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD else ThreadLoadTarget.TARGET_POSITION_LAST // Если есть непрочитанные, то грузить с первого непрочитанного, иначе с последнего сообщения в теме
                    }
                )
            }
        })

        binding.subscriptionsList.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner, { items ->
            adapter.submitList(items)
            binding.swiperefresh.setRefreshing(false)
        })

        viewModel.appAuth.authState.observe(viewLifecycleOwner, {
            binding.subscriptionsList.isVisible = it.authorized
            binding.swiperefresh.isActivated = it.authorized
            binding.notLoggedHint.isVisible = !it.authorized
        })

        binding.swiperefresh.setOnRefreshListener {
            viewModel.loadSubscriptions(globalList)
        }

        setHasOptionsMenu(true)

        return root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSubscriptions(globalList)
        viewModel.startCheckUpdates(globalList)
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
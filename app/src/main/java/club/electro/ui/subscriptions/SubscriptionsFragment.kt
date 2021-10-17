package club.electro.ui.subscriptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.adapter.FeedPostAdapter
import club.electro.adapter.OnFeedPostInteractionListener
import club.electro.adapter.SubscriptionAreaAdapter
import club.electro.adapter.SubscriptionAreaInteractionListener
import club.electro.databinding.FragmentSubscriptionsBinding
import club.electro.dto.FeedPost
import club.electro.dto.SubscriptionArea

class SubscriptionsFragment : Fragment() {

    private lateinit var subscriptionsViewModel: SubscriptionsViewModel
    private var _binding: FragmentSubscriptionsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        subscriptionsViewModel =
            ViewModelProvider(this).get(SubscriptionsViewModel::class.java)

        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = SubscriptionAreaAdapter(object : SubscriptionAreaInteractionListener {
            override fun onClick(area: SubscriptionArea) {
                findNavController().navigate(
                    R.id.action_nav_subscriptions_to_threadFragment,
//                    Bundle().apply {
//                        postId = post.id
//                    }
                )
            }
        })

        binding.subscriptionsList.adapter = adapter

        subscriptionsViewModel.data.observe(viewLifecycleOwner, { items ->
            adapter.submitList(items)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
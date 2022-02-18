package club.electro.ui.transport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.databinding.FragmentTransportBinding
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.LongArg
import club.electro.utils.load
import club.electro.utils.loadCircleCrop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransportFragment : Fragment() {
    companion object {
        var Bundle.transportId: Long by LongArg
    }

    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    private val viewModel:TransportViewModel by viewModels()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            viewModel.currentTransport.observe(viewLifecycleOwner) {
                it?.let {
                    mainViewModel.updateActionBarConfig(ToolBarConfig(
                        title = it.name,
                        subtitle = getString(R.string.owners_count) + it.users.toString(),
                        onClick = {}
                    ))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransportBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val transportId = requireArguments().transportId

        viewModel.currentTransport.observe(viewLifecycleOwner) { transport->
            binding.transportName.text = transport.name
            binding.transportRatingValue.text = transport.rating.toString()

            transport.specs?.let {
                binding.weightValue.text = it.weight.toString()
                binding.powerValue.text = it.power.toString()
            }

            transport.fullImage?.let {
                binding.transportImage.load(it)
            }
        }

        viewModel.transportDiscussions(transportId).observe(viewLifecycleOwner) {
            println("Discussions observed " + it.size)
            if (it.size > 0) {
                binding.discussionTitle.isVisible = true
                binding.discussionItem.root.isVisible = true
                val discussion = it[0]
                with (binding.discussionItem) {
                    println("Root is visible")
                    root.isVisible = true
                    discussionName.text = discussion.title
                    discussionMessagesCount.text = discussion.messages.toString()
                    discussionImage.loadCircleCrop(discussion.image)
                    //discussionLastMessageTime.text = areaLastActivityTime(discussion.lastMessageTime, requireContext())
                }

                binding.discussionItem.root.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_global_threadFragment,
                        Bundle().apply {
                            threadType = discussion.threadType
                            threadId = discussion.threadId
                            targetPostId = ThreadLoadTarget.TARGET_POSITION_FIRST
                        }
                    )
                }

            } else {
                binding.discussionTitle.isVisible = false
                binding.discussionItem.root.isVisible = false
            }
        }

        return root
    }
}
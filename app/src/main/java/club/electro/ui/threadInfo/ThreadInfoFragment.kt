package club.electro.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import club.electro.MainViewModel
import club.electro.ToolBarConfig
import club.electro.databinding.FragmentThreadInfoBinding
import club.electro.ui.threadInfo.ThreadInfoViewModelFactory
import club.electro.utils.ByteArg
import club.electro.utils.LongArg
import club.electro.utils.loadCircleCrop

class ThreadInfoFragment : Fragment() {
    companion object {
        var Bundle.threadInfoType: Byte by ByteArg
        var Bundle.threadInfoId: Long by LongArg
    }

    private var _binding: FragmentThreadInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ThreadInfoViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            viewModel.thread.observe(viewLifecycleOwner) {
                it?.let {
                    mainViewModel.updateActionBarTitle(ToolBarConfig(
                        title1 = it.name,
                    ))
                }
            }
        } ?: throw Throwable("Invalid activity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val threadType = requireArguments().threadInfoType
        val threadId = requireArguments().threadInfoId

        viewModel = ViewModelProvider(this, ThreadInfoViewModelFactory(
            requireActivity().getApplication(),
            threadType,
            threadId,
        )).get(ThreadInfoViewModel::class.java)

        viewModel.getThread()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var firstUpdateTimeReceived = false

        _binding = FragmentThreadInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.thread.observe(viewLifecycleOwner) { thread ->
            with (binding) {
                threadName.text = thread.name

                messages.text = thread.messages.toString()
                subscribers.text = thread.subscribersCount.toString()

                thread.image?.let {
                    threadImage.loadCircleCrop(it)
                }
            }
        }

        viewModel.threadStatus.observe(viewLifecycleOwner) {
            if (it.lastUpdateTime > 0L) {
                if (firstUpdateTimeReceived) {
                    viewModel.getThread()
                } else {
                    firstUpdateTimeReceived = true
                }
            }
        }

        viewModel.startCheckUpdates()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopCheckUpdates()
    }
}
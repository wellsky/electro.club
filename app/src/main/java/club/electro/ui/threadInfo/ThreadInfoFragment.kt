package club.electro.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.MainViewModel
import club.electro.ToolBarConfig
import club.electro.databinding.FragmentThreadInfoBinding
import club.electro.ui.thread.ThreadViewModel
import club.electro.utils.ByteArg
import club.electro.utils.LongArg
import club.electro.utils.loadCircleCrop
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ThreadInfoFragment : Fragment() {
    companion object {
        var Bundle.threadInfoType: Byte by ByteArg
        var Bundle.threadInfoId: Long by LongArg
    }

    private val viewModel: ThreadViewModel by viewModels()

    private var _binding: FragmentThreadInfoBinding? = null
    private val binding get() = _binding!!
    private var firstUpdateTimeReceived = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            viewModel.thread.observe(viewLifecycleOwner) {
                it?.let {
                    mainViewModel.updateActionBarConfig(ToolBarConfig(
                        title1 = it.name,
                    ))
                }
            }
        } ?: throw Throwable("Invalid activity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val threadType = requireArguments().threadInfoType
//        val threadId = requireArguments().threadInfoId

//        viewModel = ViewModelProvider(this, ThreadInfoViewModelFactory(
//            threadType,
//            threadId,
//        )).get(ThreadInfoViewModel::class.java)

        viewModel.getThread()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var firstStatusReceived = false // Должен быть объявлен во View

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
            //if (it > 0) viewModel.getThread()
            if (it.lastUpdateTime > 0) {
                if (firstStatusReceived) {
                    println("adapter.refresh")
                    viewModel.getThread()
                } else {
                    firstStatusReceived = true
                }
            }
        }

        viewModel.startCheckUpdates()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopCheckUpdates()
        _binding = null
    }
}
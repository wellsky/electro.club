package club.electro.ui.user

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
import club.electro.databinding.FragmentThreadInfoBinding
import club.electro.ui.thread.ThreadViewModel
import club.electro.ui.thread.ThreadViewModelFactory
import club.electro.ui.threadInfo.ThreadInfoViewModelFactory
import club.electro.utils.ByteArg
import club.electro.utils.LongArg
import com.bumptech.glide.Glide

class ThreadInfoFragment : Fragment() {
    companion object {
        var Bundle.threadInfoType: Byte by ByteArg
        var Bundle.threadInfoId: Long by LongArg
    }

    private var _binding: FragmentThreadInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ThreadInfoViewModel

    private var firstUpdateTimeReceived = false

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
        _binding = FragmentThreadInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.thread.observe(viewLifecycleOwner) { thread ->
            with (binding) {
                threadName.text = thread.name

                messages.text = thread.messages.toString()
                subscribers.text = thread.subscribersCount.toString()

                thread.image?.let {
                    Glide.with(threadImage.context)
                        .load(it)
                        .circleCrop()
                        .timeout(5_000)
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .into(threadImage)
                }
            }
        }

        // TODO реализовано криво. Вообще логику обновления лучше не выносить за репозиторий.
        // Но почему-то вызванная из checkForUpdates() функция reloadPosts() не релодит посты
        viewModel.lastUpdateTime.observe(viewLifecycleOwner) {
            if (it > 0L) {
                if (firstUpdateTimeReceived) {
                    viewModel.getThread()
                    //viewModel.reloadPosts()
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
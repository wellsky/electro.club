package club.electro.ui.map.socket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.databinding.FragmentSocketBinding
import club.electro.ui.thread.ThreadViewModel
import club.electro.ui.threadInfo.ThreadInfoViewModelFactory
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoId
import club.electro.ui.user.ThreadInfoFragment.Companion.threadInfoType
import club.electro.ui.user.ThreadInfoViewModel
import club.electro.utils.LongArg

class SocketFragment : Fragment() {
    companion object {
        var Bundle.socketId: Long by LongArg
    }

    private lateinit var viewModel: SocketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val socketId = requireArguments().socketId

        viewModel = ViewModelProvider(this, SocketViewModelFactory(
            requireActivity().getApplication(),
            socketId
        )
        ).get(SocketViewModel::class.java)

        //viewModel.getSocket()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSocketBinding.inflate(inflater, container, false)

        viewModel.currentSocket.observe(viewLifecycleOwner) { socket ->
            println("Socket data received in fragment: " + id)
            socket?.let {
                with(binding) {
                    threadName.setText(it.id.toString())
                }
            }
        }

        return binding.root
    }
}
package club.electro.ui.map.socket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import club.electro.databinding.FragmentSocketBinding
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSocketBinding.inflate(inflater, container, false)

        viewModel.currentSocket.observe(viewLifecycleOwner) { socket ->
            with(binding) {
                threadName.setText(socket.id.toString())
            }
        }

        return binding.root
    }
}
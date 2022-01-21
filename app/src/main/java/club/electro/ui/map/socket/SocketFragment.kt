package club.electro.ui.map.socket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.databinding.FragmentSocketBinding
import club.electro.utils.LongArg
import club.electro.utils.htmlToText
import club.electro.utils.loadCircleCrop
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SocketFragment : Fragment() {
    companion object {
        var Bundle.socketId: Long by LongArg
    }

    private val viewModel: SocketViewModel by viewModels()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val socketId = requireArguments().socketId
//
////        viewModel = ViewModelProvider(this, SocketViewModelFactory(
////            requireActivity().getApplication(),
////            socketId
////        )
////        ).get(SocketViewModel::class.java)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSocketBinding.inflate(inflater, container, false)

        viewModel.currentSocket.observe(viewLifecycleOwner) { socket ->
            with(binding) {
                authorName.text = socket.authorName
                socketDescription.text = htmlToText(socket.text)
                authorAvatar.loadCircleCrop(socket.authorAvatar)
            }
        }

        return binding.root
    }
}
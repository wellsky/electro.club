package club.electro.ui.transport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import club.electro.databinding.FragmentTransportBinding
import club.electro.utils.LongArg
import club.electro.utils.load
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransportFragment : Fragment() {
    companion object {
        var Bundle.transportId: Long by LongArg
    }

    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    private val viewModel:TransportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.currentTransport.observe(viewLifecycleOwner) { transport->
            transport.fullImage?.let {
                binding.transportImage.load(it)
            }
        }

        return root
    }
}
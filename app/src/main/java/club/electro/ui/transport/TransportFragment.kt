package club.electro.ui.transport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import club.electro.databinding.FragmentSubscriptionsBinding
import club.electro.databinding.FragmentTransportBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransportFragment : Fragment() {
    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }
}
package club.electro.ui.transport

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.MainViewModel
import club.electro.ToolBarConfig
import club.electro.adapter.TransportPreviewAdapter
import club.electro.adapter.TransportPreviewInteractionListener
import club.electro.databinding.FragmentTransportListBinding
import club.electro.dto.TransportPreview
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransportListFragment : Fragment() {
    private var _binding: FragmentTransportListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransportListViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarConfig(ToolBarConfig(
                subtitle = "",
                onClick = {}
            ))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransportListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = TransportPreviewAdapter(object : TransportPreviewInteractionListener {
            override fun onClick(transport: TransportPreview) {
                // TODO
            }
        })

        binding.transportList.adapter = adapter

        binding.search.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(filter: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filter?.let {
                    viewModel.queueNewSearch(filter.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        viewModel.transportList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package club.electro.ui.thread

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import club.electro.R
import club.electro.databinding.FragmentSubscriptionsBinding
import club.electro.databinding.FragmentThreadBinding

class ThreadFragment : Fragment() {

    companion object {
        fun newInstance() = Thread()
    }

    private lateinit var viewModel: ThreadViewModel
    private lateinit var binding: FragmentThreadBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThreadBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.fragment_thread, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ThreadViewModel::class.java)


    }

}
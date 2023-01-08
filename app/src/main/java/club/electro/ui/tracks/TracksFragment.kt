package club.electro.ui.tracks

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import club.electro.R

class TracksFragment : Fragment() {

    companion object {
        fun newInstance() = TracksFragment()
    }

    private lateinit var viewModel: TracksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TracksViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
package club.electro.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.MainViewModel
import club.electro.R
import club.electro.databinding.FragmentUserProfileBinding
import com.bumptech.glide.Glide

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by viewModels (
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root



        viewModel.currentProfile.observe(viewLifecycleOwner) {
            with (binding) {
               name.text = it.name
               messages.text = it.messages.toString()
               rating.text = it.rating.toString()

               it.avatar?.let {
                   Glide.with(avatar.context)
                       .load(it)
                       .circleCrop()
                       .timeout(5_000)
                       .placeholder(R.drawable.ic_loading_100dp)
                       .error(R.drawable.ic_error_100dp)
                       .into(avatar)
               }
            }

            activity?.run {
                val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
                mainViewModel.updateActionBarTitle(it.name)
            } ?: throw Throwable("Invalid activity")
        }

        return root
    }
}
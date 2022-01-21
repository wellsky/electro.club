package club.electro.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.databinding.FragmentUserProfileBinding
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.LongArg
import club.electro.utils.loadCircleCrop
import com.bumptech.glide.Glide

class UserProfileFragment : Fragment() {
    companion object {
        var Bundle.userId: Long by LongArg
    }

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    //private lateinit var viewModel: UserProfileViewModel

    private val viewModel: UserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val userId = requireArguments().userId

//        viewModel = ViewModelProvider(this, UserProfileViewModelFactory(
//            requireActivity().getApplication(),
//            userId
//        )).get(UserProfileViewModel::class.java)

        viewModel.currentProfile.observe(viewLifecycleOwner) { user->
            with (binding) {
                name.text = user.name

                messages.text = user.messages.toString()
                rating.text = user.rating.toString()

                transportName.isVisible = false
                transportImage.isVisible = false

                user.primaryTransport?.let { primaryTransport ->
                    transportName.text = primaryTransport.name
                    primaryTransport.image?.let {
                        transportImage.loadCircleCrop(it)
                        Glide.with(transportImage.context)
                    }
                    transportName.isVisible = true
                    transportImage.isVisible = true
                }

                user.avatar?.let {
                    avatar.loadCircleCrop(it)
               }

                chat.isVisible = false
                user.myChat?.let { link ->
                    chat.isVisible = true
                    chat.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_userProfileFragment_to_threadFragment,
                            Bundle().apply {
                                threadType = link.threadType
                                threadId = link.threadId
                            }
                        )
                    }
                }
            }

            requireActivity().run {
                val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
                mainViewModel.updateActionBarTitle(ToolBarConfig(title1 = user.name))
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
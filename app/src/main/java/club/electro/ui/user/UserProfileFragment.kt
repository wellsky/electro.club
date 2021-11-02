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
import club.electro.databinding.FragmentUserProfileBinding
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadName
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.ui.thread.ThreadViewModel
import club.electro.util.StringArg
import club.electro.utils.ByteArg
import club.electro.utils.LongArg
import com.bumptech.glide.Glide

class UserProfileFragment : Fragment() {
    companion object {
        var Bundle.userId: Long by LongArg
    }

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

//    private val viewModel: UserProfileViewModel by viewModels (
//        ownerProducer = ::requireParentFragment
//    )
    private lateinit var viewModel: UserProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val userId = arguments!!.userId!!

        viewModel = UserProfileViewModel(
            requireActivity().getApplication(),
            userId,
        )

        viewModel.getUserProfile(userId)

        viewModel.currentProfile.observe(viewLifecycleOwner) { user->
            with (binding) {
                name.text = user.name

                messages.text = user.messages.toString()
                rating.text = user.rating.toString()

                user.primaryTransport?.let {
                    transport.text = it.name
                }

                user.avatar?.let {
                Glide.with(avatar.context)
                   .load(it)
                   .circleCrop()
                   .timeout(5_000)
                   .placeholder(R.drawable.ic_loading_100dp)
                   .error(R.drawable.ic_error_100dp)
                   .into(avatar)
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
                                threadName = user.name
                            }
                        )
                    }
                }
            }

            activity?.run {
                val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
                mainViewModel.updateActionBarTitle(user.name)
            } ?: throw Throwable("Invalid activity")
        }

//        arguments?.userId?.let {
//            viewModel.getUserProfile(it)
//        }

        return root
    }
}
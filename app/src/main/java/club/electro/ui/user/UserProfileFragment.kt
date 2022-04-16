package club.electro.ui.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.databinding.FragmentUserProfileBinding
import club.electro.dto.ThreadLink
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.LongArg
import club.electro.utils.loadCircleCrop
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    companion object {
        var Bundle.userId: Long by LongArg

        @SuppressLint("SimpleDateFormat")
        private val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
    }

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by viewModels()

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
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.currentProfile.observe(viewLifecycleOwner) { user->
            with (binding) {
                name.text = user.name

                messages.text = user.messages.toString()
                rating.text = user.rating.toString()

                transportName.isVisible = false
                transportImage.isVisible = false

                user.primaryTransport?.let { primaryTransport ->
                    transportName.text = primaryTransport.name
                    primaryTransport.image.let {
                        transportImage.loadCircleCrop(it)
                        Glide.with(transportImage.context)
                    }
                    transportName.isVisible = true
                    transportImage.isVisible = true
                }

                user.avatar?.let {
                    println("Loading avatar")
                    avatar.loadCircleCrop(it)
                }


                chat.isVisible =
                     viewModel.appAuth.authorized() && viewModel.appAuth.myId() != user.id

                if (user.myChat != null) {
                    chat.setOnClickListener {
                        openChat(ThreadLink(
                            threadType = user.myChat.threadType,
                            threadId = user.myChat.threadId
                        ))
                    }
                } else {
                    chat.setOnClickListener {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage(getString(R.string.create_new_chat, user.name))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.delete_post_confirm_yes)) { dialog, id ->
                                viewModel.getChatWith(user.id).observe(viewLifecycleOwner) {
                                    if (it != null) {
                                        openChat(
                                            ThreadLink(
                                                threadType = it.threadType,
                                                threadId = it.threadId
                                            )
                                        )
                                    }
                                }
                            }
                            .setNegativeButton(getString(R.string.delete_post_confirm_no)) { dialog, id ->
                                dialog.dismiss()
                            }
                        val alert = builder.create()
                        alert.show()
                    }
                }
            }




            requireActivity().run {
                val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
                val lastVisitTime = java.util.Date(user.lastVisit * 1000)
                mainViewModel.updateActionBarConfig(ToolBarConfig(
                    title = user.name,
                    subtitle = getString(R.string.user_last_visit_time) + sdf.format(lastVisitTime).toString(),
                ))
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openChat(thread: ThreadLink) {
        findNavController().navigate(
            R.id.action_userProfileFragment_to_threadFragment,
            Bundle().apply {
                threadType = thread.threadType
                threadId = thread.threadId
                targetPostId = ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD
            }
        )
    }
}
package club.electro.ui.map.socket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.databinding.FragmentSocketBinding
import club.electro.dto.Socket
import club.electro.dto.SocketStatus
import club.electro.ui.user.UserProfileFragment.Companion.userId
import club.electro.utils.LongArg
import club.electro.utils.load
import club.electro.utils.loadCircleCrop
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SocketFragment : Fragment() {
    companion object {
        var Bundle.socketId: Long by LongArg
        private val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
    }

    private val viewModel: SocketViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSocketBinding.inflate(inflater, container, false)

        viewModel.currentSocket.observe(viewLifecycleOwner) { socket ->
            if (socket != null) {
                with(binding) {
                    authorName.text = socket.authorName
                    socketDescription.text = socket.text
                    authorAvatar.loadCircleCrop(socket.authorAvatar)

                    val createdTime = java.util.Date(socket.created * 1000)
                    created.text = sdf.format(createdTime).toString()

                    statusValue.text = socket.status.toName()

                    socket.images?.let {
                        socketImage.isVisible = true
                        if (it.size > 1) {
                            moreImagesDiv.isVisible = true
                            moreImagesText.text =
                                getString(R.string.zoom_more_images, (it.size - 1).toString())
                        }

                        socketImage.load(it[0])

                        socketImage.setOnClickListener {
                            showSocketImages(socket)
                        }
                    }

                    authorAvatar.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_global_userProfileFragment,
                            Bundle().apply {
                                userId = socket.authorId
                            }
                        )
                    }

                    buttonOn.setOnClickListener {
                        confirmSettingStatus(socket, SocketStatus.ON)
                    }
                    buttonOff.setOnClickListener {
                        confirmSettingStatus(socket, SocketStatus.OFF)
                    }
                    buttonMissing.setOnClickListener {
                        confirmSettingStatus(socket, SocketStatus.MISSING)
                    }
                }
            }
        }

        return binding.root
    }

    private fun SocketStatus.toName(): String =
        when (this) {
            SocketStatus.ON -> getString(R.string.socket_status_on)
            SocketStatus.OFF -> getString(R.string.socket_status_off)
            SocketStatus.MISSING -> getString(R.string.socket_status_missing)
        }

    private fun showSocketImages(socket: Socket) {
        socket.images?.let {
            StfalconImageViewer.Builder(context, it) { view, image ->
                Picasso.get().load(image).into(view)
            }.show()
        }
    }

    private fun confirmSettingStatus(socket: Socket, status: SocketStatus) {
        val textId = if (socket.status == status)
            R.string.confirm_confirm_socket_status
        else
            R.string.confirm_change_socket_status

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(textId, status.toName()))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.confirm_yes)) { _, _ ->
                viewModel.setSocketStatus(socket.id, status)
            }
            .setNegativeButton(getString(R.string.confirm_no)) { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
}
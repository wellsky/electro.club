package club.electro.ui.map.socket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.adapter.FeedPostViewHolder
import club.electro.databinding.FragmentSocketBinding
import club.electro.dto.Socket
import club.electro.ui.user.UserProfileFragment.Companion.userId
import club.electro.utils.LongArg
import club.electro.utils.htmlToText
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

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val socketId = requireArguments().socketId
//
////        viewModel = ViewModelProvider(this, SocketViewModelFactory(
////            requireActivity().getApplication(),
////            socketId
////        )
////        ).get(SocketViewModel::class.java)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSocketBinding.inflate(inflater, container, false)

        viewModel.currentSocket.observe(viewLifecycleOwner) { socket ->
            with(binding) {
                authorName.text = socket.authorName
                socketDescription.text = htmlToText(socket.text)
                authorAvatar.loadCircleCrop(socket.authorAvatar)

                val createdTime = java.util.Date(socket.created * 1000)
                created.text = sdf.format(createdTime).toString()

                statusValue.text = when (socket.status) {
                    "off" -> getString(R.string.socket_status_off)
                    "missing" -> getString(R.string.socket_status_missing)
                    else -> getString(R.string.socket_status_on)
                }

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
            }
        }

        return binding.root
    }

    fun showSocketImages(socket: Socket) {
        socket.images?.let {
            StfalconImageViewer.Builder(context, it) { view, image ->
                Picasso.get().load(image).into(view)
            }.show()
        }
    }
}
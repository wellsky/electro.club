package club.electro.ui.thread

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.adapter.PostAttachmentAdapter
import club.electro.adapter.PostAttachmentInteractionListener
import club.electro.adapter.TransportPreviewAdapter
import club.electro.adapter.TransportPreviewInteractionListener
import club.electro.databinding.FragmentPostAttachmentsBinding
import club.electro.dto.PostAttachment
import club.electro.dto.TransportPreview
import club.electro.ui.transport.TransportFragment.Companion.transportId
import club.electro.util.AndroidUtils
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.features.registerImagePicker
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PostAttachmentsFragment: Fragment(R.layout.fragment_post_attachments) {
    private val viewModel: PostAttachmentsViewModel by viewModels()

    private var _binding: FragmentPostAttachmentsBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostAttachmentsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = PostAttachmentAdapter(object : PostAttachmentInteractionListener {
            override fun onClick(attachment: PostAttachment) {
                AndroidUtils.hideKeyboard(requireView())
            }
        })

        binding.attachmentsList.adapter = adapter

        viewModel.attachments.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // TODO не создавать объекты внутри onCreateView?
        val imagePickerLauncher = registerImagePicker {
            val firstImage = it.firstOrNull() ?: return@registerImagePicker
            it.forEach { image ->
                viewModel.queueAttachment(image.path)
            }
        }

        binding.fabAdd.setOnClickListener {
            imagePickerLauncher.launch(
                ImagePickerConfig {
                    mode = ImagePickerMode.MULTIPLE
                    //returnMode = ReturnMode.ALL // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                    isFolderMode = true // set folder mode (false by default)
                    folderTitle = "Folder" // folder selection title
                    imageTitle = "Tap to select" // image selection title
                    doneButtonText = "DONE" // done button text
                }
            )
        }

        return root
    }
}
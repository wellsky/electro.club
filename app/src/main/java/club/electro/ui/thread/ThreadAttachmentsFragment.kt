package club.electro.ui.thread

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.adapter.PostAttachmentAdapter
import club.electro.adapter.PostAttachmentInteractionListener
import club.electro.databinding.FragmentThreadAttachmentsBinding
import club.electro.dto.PostAttachment
import club.electro.utils.ByteArg
import club.electro.utils.LongArg
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.registerImagePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThreadAttachmentsFragment: Fragment(R.layout.fragment_thread_attachments) {
    private val viewModel: ThreadViewModel by viewModels()

    private var _binding: FragmentThreadAttachmentsBinding? = null
    private val binding get() = _binding!!

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThreadAttachmentsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = PostAttachmentAdapter(object : PostAttachmentInteractionListener {
            override fun onRemoveClick(attachment: PostAttachment) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(getString(R.string.delete_post_attachment_confirm_text))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.delete_post_confirm_yes)) { dialog, id ->
                        viewModel.removeAttachment(attachment)
                    }
                    .setNegativeButton(getString(R.string.delete_post_confirm_no)) { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        })

        binding.attachmentsList.adapter = adapter


        viewModel.editorAttachments.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // TODO не создавать объекты внутри onCreateView?
        val imagePickerLauncher = registerImagePicker {
            it.forEach { image ->
                viewModel.queueAttachment(image.name, image.path)
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
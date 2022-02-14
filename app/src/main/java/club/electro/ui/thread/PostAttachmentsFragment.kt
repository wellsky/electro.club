package club.electro.ui.thread

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import club.electro.R
import club.electro.databinding.FragmentPostAttachmentsBinding
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

        val imagePickerLauncher = registerImagePicker {
            val firstImage = it.firstOrNull() ?: return@registerImagePicker
            it.forEach { image ->
                //println(image.path)
                //val file = File(image.path)
                viewModel.queueAttachment(image.path)
            }

            //viewModel.startAll()
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
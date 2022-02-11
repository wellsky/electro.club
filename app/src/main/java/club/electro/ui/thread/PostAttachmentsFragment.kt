package club.electro.ui.thread

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import club.electro.databinding.FragmentPostAttachmentsBinding
import club.electro.utils.ByteArg
import club.electro.utils.LongArg
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostAttachmentsFragment: Fragment() {
    companion object {
        var Bundle.threadType: Byte by ByteArg
        var Bundle.threadId: Long by LongArg
        var Bundle.postId: Long by LongArg // Может быть -1 (загрузить с последнего сообщения) и -2 (с первого непрочитанного)
    }

    private val viewModel: PostAttachmentsViewModel by viewModels()

    private var _binding: FragmentPostAttachmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostAttachmentsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        uri?.let {
                            viewModel.addAttachment(it)
                        }
                    }
                }
            }

        binding.fabAdd.setOnClickListener {
            ImagePicker.with(this)
                .compress(1920)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        return root
    }
}
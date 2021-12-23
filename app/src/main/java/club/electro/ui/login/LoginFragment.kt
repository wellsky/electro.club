import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import club.electro.R
import club.electro.auth.AppAuth
import club.electro.databinding.FragmentLoginBinding
import club.electro.util.AndroidUtils
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.viewmodel.LoginFormState
import ru.netology.nmedia.viewmodel.LoginViewModel
import androidx.fragment.app.activityViewModels
import club.electro.utils.loadCircleCrop


class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels (
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.submit.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            viewModel.signIn(
                binding.login.editText?.text.toString().trim(),
                binding.password.editText?.text.toString().trim()
            )
        }

        binding.logout.setOnClickListener {
            viewModel.signOut()
        }

        viewModel.state.observe(viewLifecycleOwner, {
            binding.loggedUserGroup.isVisible = false
            binding.loginFormGroup.isVisible = true

            when (it) {
                LoginFormState.SUCCESS, LoginFormState.LOGGED -> {
                    binding.loggedUserGroup.isVisible = true
                    binding.loginFormGroup.isVisible = false

                    val appAuth = AppAuth.getInstance()
                    binding.userName.text = appAuth.myName()

                    appAuth.myAvatar()?.let {
                        binding.userAvatar.loadCircleCrop(appAuth.myAvatar())
                    }
                }
                LoginFormState.SUCCESS -> {
                    AndroidUtils.hideKeyboard(requireView())
                    Snackbar.make(binding.root, "Success!", Snackbar.LENGTH_LONG)
                        .show()
                    //findNavController().navigateUp()
                }
                LoginFormState.ERROR -> {
                    AndroidUtils.hideKeyboard(requireView())
                    Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        })

        return binding.root
    }
}
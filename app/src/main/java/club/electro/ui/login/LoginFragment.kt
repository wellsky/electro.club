package club.electro.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import club.electro.R
import club.electro.auth.AppAuth
import club.electro.databinding.FragmentFeedBinding
import club.electro.databinding.FragmentLoginBinding
import club.electro.util.AndroidUtils
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.viewmodel.LoginFormState
import ru.netology.nmedia.viewmodel.LoginViewModel
import club.electro.utils.loadCircleCrop


class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels (
        ownerProducer = ::requireParentFragment
    )

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

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
                LoginFormState.LOGGED -> {
                    setUserLogged()
                }
                LoginFormState.SUCCESS -> {
                    setUserLogged()
                    AndroidUtils.hideKeyboard(requireView())
                    Snackbar.make(binding.root, R.string.success_auth, Snackbar.LENGTH_LONG)
                        .show()
                }
                LoginFormState.NOT_LOGGED -> {
                    AndroidUtils.hideKeyboard(requireView())
                    Snackbar.make(binding.root, R.string.success_auth, Snackbar.LENGTH_LONG)
                        .show()
                }
                LoginFormState.ERROR -> {
                    AndroidUtils.hideKeyboard(requireView())
                    Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_LONG)
                        .show()
                }
                else -> {

                }
            }
        })

        return binding.root
    }

    fun setUserLogged() {
        binding.loggedUserGroup.isVisible = true
        binding.loginFormGroup.isVisible = false

        val appAuth = AppAuth.getInstance()
        binding.userName.text = appAuth.myName()

        appAuth.myAvatar()?.let {
            binding.userAvatar.loadCircleCrop(appAuth.myAvatar())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
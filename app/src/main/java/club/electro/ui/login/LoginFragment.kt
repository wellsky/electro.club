package club.electro.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.auth.AppAuth
import club.electro.databinding.FragmentLoginBinding
import club.electro.utils.AndroidUtils
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.viewmodel.LoginFormState
import ru.netology.nmedia.viewmodel.LoginViewModel
import club.electro.utils.loadCircleCrop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: Fragment() {
    private val viewModel: LoginViewModel by viewModels (
        ownerProducer = ::requireParentFragment
    )

    private var _binding: FragmentLoginBinding? = null
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
                    setUserLogged(viewModel.appAuth)
                }
                LoginFormState.SUCCESS -> {
                    setUserLogged(viewModel.appAuth)
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

    fun setUserLogged(appAuth: AppAuth) {
        binding.loggedUserGroup.isVisible = true
        binding.loginFormGroup.isVisible = false

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
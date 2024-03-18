package com.example.venues.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.venues.R
import com.example.venues.data.model.User
import com.example.venues.data.remote.Status
import com.example.venues.databinding.FragmentLoginBinding
import com.example.venues.utils.Util.showSnackBar
import com.example.venues.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val loginViewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        initializeViewModel()
        listeners()

        return binding.root
    }

    private fun initializeViewModel() {
        getLoadingObserver()
        getLoginObserver()
    }

    private fun getLoadingObserver() {
        loginViewModel.loadingStateLiveData.observe(viewLifecycleOwner) {
            binding.layoutLoading.root.visibility =
                if (it) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun getLoginObserver() {
        loginViewModel.currentUserLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    requireActivity().startActivity(
                        Intent(requireActivity(), MainActivity::class.java)
                    )
                    requireActivity().finish()
                }

                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun listeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                getUser()?.let { user ->
                    loginViewModel.login(user)
                }
            }

            btnRegister.setOnClickListener {
                Navigation.findNavController(it)
                    .navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }
    }

    private fun getUser(): User? {
        val user: User?

        binding.apply {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            user = if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                parent.showSnackBar(getString(R.string.enter_email))
                null
            } else if (password.length < 8) {
                parent.showSnackBar(getString(R.string.enter_password))
                null
            } else {
                User(
                    email = email,
                    password = password
                )
            }
        }

        return user
    }

}
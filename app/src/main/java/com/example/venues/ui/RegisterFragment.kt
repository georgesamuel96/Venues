package com.example.venues.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.venues.R
import com.example.venues.data.model.User
import com.example.venues.data.remote.Status
import com.example.venues.databinding.FragmentRegisterBinding
import com.example.venues.viewmodel.LoginViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val loginViewModel: LoginViewModel by viewModels()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var selectedBirthDay = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        initializeViewModel()
        listeners()

        return binding.root
    }

    private fun initializeViewModel() {
        getLoadingObserver()
        getRegisterObserver()
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

    private fun getRegisterObserver() {
        loginViewModel.currentUserLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val user = getUser(excludePassword = true)
                    requireActivity().startActivity(
                        Intent(
                            requireContext(),
                            MainActivity::class.java
                        )
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
            btnDate.setOnClickListener {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setTitleText(getString(R.string.select_date))
                        .setSelection(selectedBirthDay)
                        .build()

                picker.show(
                    requireActivity().supportFragmentManager,
                    RegisterFragment::class.simpleName
                )

                picker.addOnPositiveButtonClickListener {
                    val days = getDateDiffInDays(
                        Date(it),
                        Date(System.currentTimeMillis())
                    )
                    if (days < 18 * 365) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.age_less_than_required),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        selectedBirthDay = it
                        btnDate.text = getBirthdate()
                    }
                }
            }

            btnRegister.setOnClickListener {
                loginViewModel.register(
                    getUser()
                )
            }
        }
    }

    private fun getUser(excludePassword: Boolean = false): User {
        val user: User

        binding.apply {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val birthdate = getBirthdate()

            user = User(firstName, lastName, email, birthdate, password = if(excludePassword) null else password)
        }

        return user
    }

    private fun getBirthdate(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH) // Set the desired date format
        val date = Date(selectedBirthDay)
        return sdf.format(date)
    }

    private fun getDateDiffInDays(startDate: Date, endDate: Date): Long {
        val diff = endDate.time - startDate.time
        val daysInMs = 1000 * 60 * 60 * 24
        return diff / daysInMs
    }
}
package com.example.venues.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.venues.R
import com.example.venues.data.model.User
import com.example.venues.data.remote.Status
import com.example.venues.databinding.FragmentHomeBinding
import com.example.venues.databinding.FragmentProfileBinding
import com.example.venues.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileViewModel.getUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        initializeViewModel()

        return binding.root
    }

    private fun initializeViewModel() {
        profileViewModel.userLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val user = it.data!!
                    setUserData(user)
                }

                Status.ERROR -> {

                }
            }
        }
    }

    private fun setUserData(user: User) {
        binding.apply {
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etEmail.setText(user.email)
            btnDate.text = user.birthdate
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
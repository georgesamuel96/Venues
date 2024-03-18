package com.example.venues.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.venues.R
import com.example.venues.databinding.FragmentRegisterBinding
import com.google.android.material.datepicker.MaterialDatePicker

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var selectedBirthDay = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        listeners()

        return binding.root
    }

    private fun listeners() {
        binding.apply {
            btnDate.setOnClickListener {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setTitleText(getString(R.string.select_date))
                        .setSelection(selectedBirthDay)
                        .build()

                picker.show(requireActivity().supportFragmentManager, RegisterFragment::class.simpleName)

                picker.addOnPositiveButtonClickListener {
                    selectedBirthDay = it
                }
            }

            btnRegister.setOnClickListener {

            }
        }
    }
}
package com.example.project2.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project2.databinding.RegisterBinding
import com.example.project2.utils.Resource
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.project2.R
import com.example.project2.data.repository.firebaseImpl.AuthRepositoryFirebase
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared


class RegisterFragment : Fragment(){

    private var binding : RegisterBinding by autoCleared()
    private val viewModel:RegisterViewModel by viewModels(){
        RegisterViewModel.RegisterViewModelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterBinding.inflate(inflater, container, false)

        binding.userRegisterButton.setOnClickListener {
            val username = binding.edxtusername.text.toString()

            val email = binding.edxtemail.text.toString()
            val password = binding.edxtpassword.text.toString()
            val confirmPassword = binding.edxtConfirmPassword.text.toString()

            // ✅ בדיקת אימייל בפורמט חוקי
            if (email.isNullOrEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(),
                    getString(R.string.please_enter_a_valid_email_address), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isNullOrEmpty() || password.length < 6) {
                Toast.makeText(requireContext(),
                    getString(R.string.password_must_be_at_least_6_characters), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(),
                    getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ שליחה של אימייל נכון
            viewModel.createUser(username ?: "", email, password, confirmPassword ?: "")
        }

        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userRegistrationStatus.observe(viewLifecycleOwner) { it ->

            when(it.status) {
                Resource.Status.LOADING -> {
                    binding.registerProgress.isVisible = true
                    binding.userRegisterButton.isEnabled = false
                }
                Resource.Status.SUCCESS -> {
                    Toast.makeText(requireContext(),
                        getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_categoriesFragment)
                }
                Resource.Status.ERROR -> {
                    binding.registerProgress.isVisible = false
                    binding.userRegisterButton.isEnabled = true
                    Toast.makeText(requireContext(), it.message ?: getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}
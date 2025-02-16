package com.example.project2.ui.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project2.R
import com.example.project2.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.project2.databinding.LoginLayoutBinding
import com.example.project2.utils.Resource
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared


class LoginFragment : Fragment() {

    private var binding : LoginLayoutBinding by autoCleared()
    private val viewModel : LoginViewModel by viewModels {
        LoginViewModel.LoginViewModelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginLayoutBinding.inflate(inflater,container,false)



        binding.buttonLogin.setOnClickListener {

            viewModel.signInUser(binding.email.text.toString(),
                binding.password.text.toString())
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userSignInStatus.observe(viewLifecycleOwner) {

            when(it.status) {
                Resource.Status.LOADING-> {
                    binding.loginProgressBar.isVisible = true
                    binding.buttonLogin.isEnabled = false
                }
                Resource.Status.SUCCESS -> {
                    Toast.makeText(requireContext(),"Login successful", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_categoriesFragment)
                }
                Resource.Status.ERROR -> {
                    binding.loginProgressBar.isVisible = false
                    binding.buttonLogin.isEnabled = true
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {

            when(it.status) {
                Resource.Status.LOADING-> {
                    binding.loginProgressBar.isVisible = true
                    binding.buttonLogin.isEnabled = false
                }
                Resource.Status.SUCCESS -> {
                    findNavController().navigate(R.id.action_loginFragment_to_categoriesFragment)
                }
                Resource.Status.ERROR  -> {
                    binding.loginProgressBar.isVisible = false
                    binding.buttonLogin.isEnabled = true
                }
            }
        }
    }
}
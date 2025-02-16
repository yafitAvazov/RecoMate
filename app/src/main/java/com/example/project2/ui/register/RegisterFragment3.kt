//package il.co.syntax.firebasemvvm.ui.register
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.core.view.isVisible
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import il.co.syntax.firebasemvvm.R
//import il.co.syntax.firebasemvvm.databinding.FragmentLoginBinding
//import il.co.syntax.firebasemvvm.databinding.FragmentRegisterBinding
//import il.co.syntax.firebasemvvm.repository.FirebaseImpl.AuthRepositoryFirebase
//import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared
//import il.co.syntax.myapplication.util.Resource
//
//class RegisterFragment : Fragment(){
//
//    private var binding : FragmentRegisterBinding by autoCleared()
//    private val viewModel : RegisterViewModel by viewModels() {
//        RegisterViewModel.RegisterViewModelFactory(AuthRepositoryFirebase())
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentRegisterBinding.inflate(inflater,container,false)
//
//        binding.userRegisterButton.setOnClickListener {
//
//            viewModel.createUser(binding.edxtUserName.editText?.text.toString(),
//             binding.edxtEmailAddress.editText?.text.toString(),
//             binding.edxtPhoneNum.editText?.text.toString(),
//             binding.edxtPassword.editText?.text.toString())
//        }
//        return binding.root
//    }
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel.userRegistrationStatus.observe(viewLifecycleOwner) {
//
//            when(it) {
//                is Resource.Loading -> {
//                    binding.registerProgress.isVisible = true
//                    binding.userRegisterButton.isEnabled = false
//                }
//                is Resource.Success -> {
//                    Toast.makeText(requireContext(),"Registration successful",Toast.LENGTH_SHORT).show()
//                    findNavController().navigate(R.id.action_registerFragment_to_allTasksFragment2)
//                }
//                is Resource.Error -> {
//                    binding.registerProgress.isVisible = false
//                    binding.userRegisterButton.isEnabled = true
//                }
//            }
//        }
//
//    }
//}
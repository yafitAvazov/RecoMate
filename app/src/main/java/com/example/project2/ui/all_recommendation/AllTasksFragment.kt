//package il.co.syntax.firebasemvvm.ui.all_tasks
//
//import android.os.Bundle
//import android.view.*
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.view.isVisible
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.android.material.snackbar.Snackbar
//import il.co.syntax.firebasemvvm.R
//import il.co.syntax.firebasemvvm.databinding.AddTaskDialogBinding
//import il.co.syntax.firebasemvvm.databinding.FragmentAllTasksBinding
//import il.co.syntax.firebasemvvm.databinding.FragmentLoginBinding
//import il.co.syntax.firebasemvvm.model.Task
//import il.co.syntax.firebasemvvm.repository.FirebaseImpl.AuthRepositoryFirebase
//import il.co.syntax.firebasemvvm.repository.FirebaseImpl.TaskRepositoryFirebase
//import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared
//import il.co.syntax.myapplication.util.Resource
//
//class AllTasksFragment : Fragment() {
//
//    private var binding : FragmentAllTasksBinding by autoCleared()
//    private val viewModel : AllTasksViewModel by viewModels {
//        AllTasksViewModel.AllTaskViewModelFactory(AuthRepositoryFirebase(),TaskRepositoryFirebase())
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        setHasOptionsMenu(true)
//
//        binding = FragmentAllTasksBinding.inflate(inflater,container,false)
//
//        binding.fab.setOnClickListener {
//
//            val binding : AddTaskDialogBinding = AddTaskDialogBinding.inflate(inflater)
//
//            AlertDialog.Builder(requireContext())
//                .setView(binding.root)
//                .setPositiveButton("Add") {
//                    p0, p1 ->
//                    viewModel.addTask(binding.missionTxt.text.toString())
//                }.show()
//        }
//        return binding.root
//    }
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
//
//        binding.recycler.adapter = TasksAdapter(object : TasksAdapter.TaskListener {
//
//            override fun onTaskClicked(task: Task) {
//                viewModel.setCompleted(task.id,!task.finished)
//            }
//
//            override fun onTaskLongClicked(task: Task) {
//                viewModel.deleteTask(task.id)
//            }
//        })
//
//        viewModel.taskStatus.observe(viewLifecycleOwner) {
//            when(it) {
//                is Resource.Loading -> {
//                    binding.progressBar.isVisible = true
//                    binding.fab.isEnabled = false
//                }
//                is Resource.Success -> {
//                    binding.progressBar.isVisible = false
//                    binding.fab.isEnabled = true
//                    (binding.recycler.adapter as TasksAdapter).setTasks(it.data!!)
//                }
//                is Resource.Error -> {
//                    binding.progressBar.isVisible = false
//                    binding.fab.isEnabled = true
//                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
//                }
//
//            }
//        }
//
//        viewModel.addTaskStatus.observe(viewLifecycleOwner) {
//            when(it) {
//                is Resource.Loading -> {
//                    binding.progressBar.isVisible = true
//                }
//                is Resource.Success -> {
//                    binding.progressBar.isVisible = false
//                    Snackbar.make(binding.coordinator,"Item Added!",Snackbar.LENGTH_SHORT).show()
//                }
//                is Resource.Error -> {
//                    binding.progressBar.isVisible = false
//                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
//                }
//
//            }
//        }
//
//        viewModel.deleteTaskStatus.observe(viewLifecycleOwner) {
//            when(it) {
//                is Resource.Loading -> {
//                    binding.progressBar.isVisible = true
//                }
//                is Resource.Success -> {
//                    binding.progressBar.isVisible = false
//                    Snackbar.make(binding.coordinator,"Item Deleted!",Snackbar.LENGTH_SHORT)
//                        .setAction("Undo") {
//                            Toast.makeText(requireContext(),"For you to implement",Toast.LENGTH_SHORT).show()
//                        }.show()
//                }
//                is Resource.Error -> {
//                    binding.progressBar.isVisible = false
//                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
//                }
//
//            }
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.main_menu,menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId == R.id.action_sign_out) {
//            viewModel.signOut()
//            findNavController().navigate(R.id.action_allTasksFragment2_to_loginFragment)
//        }
//        return super.onOptionsItemSelected(item)
//    }
//}
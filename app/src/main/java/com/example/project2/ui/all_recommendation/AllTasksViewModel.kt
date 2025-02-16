//package il.co.syntax.firebasemvvm.ui.all_tasks
//
//import androidx.lifecycle.*
//import il.co.syntax.firebasemvvm.model.Task
//import il.co.syntax.firebasemvvm.repository.AuthRepository
//import il.co.syntax.firebasemvvm.repository.TasksRepository
//import il.co.syntax.myapplication.util.Resource
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.flowOn
//import kotlinx.coroutines.launch
//
//class AllTasksViewModel(private val authRep:AuthRepository, val taskRep:TasksRepository) : ViewModel() {
//
//    //With LiveData
//    private val _tasksStatus : MutableLiveData<Resource<List<Task>>> = MutableLiveData()
//    val taskStatus: LiveData<Resource<List<Task>>> = _tasksStatus
//
//    //With Flow
//    //val taskStatus: LiveData<Resource<List<Task>>> = taskRep.getTasksFlow().flowOn(Dispatchers.IO).asLiveData()
//
//    private val _addTaskStatus = MutableLiveData<Resource<Void>>()
//    val addTaskStatus:LiveData<Resource<Void>> = _addTaskStatus
//
//    private val _deleteTaskStatus = MutableLiveData<Resource<Void>>()
//    val deleteTaskStatus:LiveData<Resource<Void>> = _deleteTaskStatus
//
//   /* init {
//        taskRep.getTasksLiveData(_tasksStatus)
//    }*/
//    fun signOut() {
//        authRep.logout()
//    }
//
//    fun addTask(title:String) {
//        viewModelScope.launch {
//            if(title.isEmpty())
//                _addTaskStatus.postValue(Resource.Error("Empty task title"))
//            else {
//                _addTaskStatus.postValue(Resource.Loading())
//                _addTaskStatus.postValue(taskRep.addTask(title))
//            }
//        }
//    }
//
//    fun deleteTask(id:String) {
//        viewModelScope.launch {
//            if(id.isEmpty())
//                _deleteTaskStatus.postValue(Resource.Error("Empty task id"))
//            else {
//                _deleteTaskStatus.postValue(Resource.Loading())
//                _deleteTaskStatus.postValue(taskRep.deleteTask(id))
//            }
//        }
//    }
//
//    fun setCompleted(id:String, boolean: Boolean) {
//        viewModelScope.launch {
//            taskRep.setCompleted(id,boolean)
//        }
//    }
//
//    class AllTaskViewModelFactory(val authRepo:AuthRepository, val taskRep:TasksRepository) : ViewModelProvider.NewInstanceFactory() {
//
//        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//            return AllTasksViewModel(authRepo,taskRep) as T
//        }
//    }
//}
package com.example.project2.ui.login

import androidx.lifecycle.*
import com.example.project2.data.model.User
import com.example.project2.data.repository.AuthRepository
import com.example.project2.utils.Resource
import kotlinx.coroutines.launch

class LoginViewModel(private val authRep: AuthRepository) : ViewModel() {

    private val _userSignInStatus = MutableLiveData<Resource<User>>()
    val userSignInStatus: LiveData<Resource<User>> = _userSignInStatus

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser:LiveData<Resource<User>> = _currentUser

    init {
        viewModelScope.launch {
            _currentUser.postValue(Resource.loading())
            _currentUser.postValue(authRep.currentUser())
        }
    }

    fun signInUser(userEmail:String, userPass:String) {
        if(userEmail.isEmpty() || userPass.isEmpty())
            _userSignInStatus.postValue(Resource.error("Empty email or password"))
        else{
            _userSignInStatus.postValue(Resource.loading())
            viewModelScope.launch {
                val loginResult = authRep.login(userEmail,userPass)
                _userSignInStatus.postValue(loginResult)
            }
        }
    }


    class LoginViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(repo) as T
        }
    }
}
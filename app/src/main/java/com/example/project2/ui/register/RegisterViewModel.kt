package com.example.project2.ui.register

import android.util.Patterns
import androidx.lifecycle.*
import com.example.project2.data.model.User
import com.example.project2.data.repository.AuthRepository
import com.example.project2.utils.Resource

import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _userRegistrationStatus = MutableLiveData<Resource<User>>()
    val userRegistrationStatus: LiveData<Resource<User>> = _userRegistrationStatus

    fun createUser(userName:String, email:String,  userLoginPass: String,
                   userLoginPassAgain: String) {
        val error = if(email.isEmpty() || userName.isEmpty() || userLoginPass.isEmpty() || userLoginPassAgain.isEmpty())
            "Empty fields"

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Not a valid email"
        }else null
        error?.let {
            _userRegistrationStatus.postValue(Resource.error(it))
        }
        _userRegistrationStatus.value = Resource.loading()
        viewModelScope.launch {
            val registrationResult = repository.createUser(userName,email,userLoginPass,userLoginPassAgain)
            _userRegistrationStatus.postValue(registrationResult)
        }

    }

    class RegisterViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RegisterViewModel(repo) as T
        }
    }
}
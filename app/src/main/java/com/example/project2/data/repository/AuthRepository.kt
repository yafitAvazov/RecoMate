package com.example.project2.data.repository

import com.example.project2.data.model.User
import com.example.project2.utils.Resource


interface AuthRepository {

    suspend fun currentUser() : Resource<User>
    suspend fun login(userName:String, password:String) : Resource<User>
    suspend fun createUser(userName:String,
                           email:String,
                           userLoginPass:String,
                           userLoginPassAgain:String)
    : Resource<User>
    fun logout()
}
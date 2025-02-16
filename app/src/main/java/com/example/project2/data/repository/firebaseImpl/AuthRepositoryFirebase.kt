package com.example.project2.data.repository.firebaseImpl

import com.example.project2.data.model.User
import com.example.project2.data.repository.AuthRepository
import com.example.project2.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext
import safeCall


class AuthRepositoryFirebase : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userRef = FirebaseFirestore.getInstance().collection("users")

    override suspend fun currentUser(): Resource<User> {
       return withContext(Dispatchers.IO) {
           safeCall {
               val user = userRef.document(firebaseAuth.currentUser!!.uid).get().await().toObject(User::class.java)
               Resource.success(user!!)
           }
       }
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result  = firebaseAuth.signInWithEmailAndPassword(email,password).await()
                val user = userRef.document(result.user?.uid!!).get().await().toObject(User::class.java)!!
                Resource.success(user)
            }
        }
    }

    override suspend fun createUser(
        userName: String,
        email: String,  // ✅ שימוש נכון באימייל
        userLoginPass: String,
        userLoginPassAgain: String
    ): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                // ✅ לוודא שה- email נשלח, לא ה- userName
                val registrationResult  = firebaseAuth.createUserWithEmailAndPassword(userName, userLoginPass).await()
                val userId = registrationResult.user?.uid!!
                val newUser = User(userName, email) // ✅ משתמש עם שם ואימייל
                userRef.document(userId).set(newUser).await()
                Resource.success(newUser)
            }
        }
    }


    override fun logout() {
        firebaseAuth.signOut()
    }
}
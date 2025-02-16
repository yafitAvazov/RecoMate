//package com.example.project2.data.repository.FirebaseImpl
//
//import com.example.project2.data.model.User
//import com.example.project2.data.repository.AuthRepository
//import com.example.project2.utils.Resource
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.withContext
//
//
//class AuthRepositoryFirebase : AuthRepository {
//
//    private val firebaseAuth = FirebaseAuth.getInstance()
//    private val userRef = FirebaseFirestore.getInstance().collection("users")
//
//    override suspend fun currentUser(): Resource<User> {
//       return withContext(Dispatchers.IO) {
//           safeCall {
//               val user = userRef.document(firebaseAuth.currentUser!!.uid).get().await().toObject(User::class.java)
//               Resource.Success(user!!)
//           }
//       }
//    }
//
//    override suspend fun login(email: String, password: String): Resource<User> {
//        return withContext(Dispatchers.IO) {
//            safeCall {
//                val result  = firebaseAuth.signInWithEmailAndPassword(email,password).await()
//                val user = userRef.document(result.user?.uid!!).get().await().toObject(User::class.java)!!
//                Resource.Success(user)
//            }
//        }
//    }
//
//    override suspend fun createUser(
//        userName: String,
//        userLoginPass: String
//    ) : Resource<User> {
//        return withContext(Dispatchers.IO) {
//            safeCall {
//                val registrationResult  = firebaseAuth.createUserWithEmailAndPassword(userEmail,userLoginPass).await()
//                val userId = registrationResult.user?.uid!!
//                val newUser = User(userName,userEmail,userPhone)
//                userRef.document(userId).set(newUser).await()
//                Resource.Success(newUser)
//            }
//
//        }
//
//
//    }
//
//    override fun logout() {
//        firebaseAuth.signOut()
//    }
//}
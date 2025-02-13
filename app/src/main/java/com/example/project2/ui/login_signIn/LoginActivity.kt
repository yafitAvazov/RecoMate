//package com.example.project2.ui.login_signIn
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.TextUtils
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.project2.R
//import com.example.project2.ui.MainActivity
//import com.google.firebase.auth.FirebaseAuth
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var loginEmail: EditText
//    private lateinit var loginPassword: EditText
//    private lateinit var loginButton: Button
//    private lateinit var firebaseAuth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.login_layout)
//
//        // קישור ה-XML לקוד
//        loginEmail = findViewById(R.id.username)
//        loginPassword = findViewById(R.id.password)
//        loginButton = findViewById(R.id.Sign_Up_btn)
//
//        firebaseAuth = FirebaseAuth.getInstance()
//
//        // מאזין להתחברות עם אימייל וסיסמה
//        loginButton.setOnClickListener {
//            val email = loginEmail.text.toString().trim()
//            val password = loginPassword.text.toString().trim()
//
//            if (TextUtils.isEmpty(email)) {
//                showToast("Please enter your email")
//                return@setOnClickListener
//            }
//            if (TextUtils.isEmpty(password)) {
//                showToast("Please enter your password")
//                return@setOnClickListener
//            }
//
//            firebaseAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this) { task ->
//                    if (task.isSuccessful) {
//                        showToast("Login successful!")
//                        startActivity(Intent(this, MainActivity::class.java))
//                        finish()
//                    } else {
//                        showToast("Login failed: ${task.exception?.message}")
//                    }
//                }
//        }
//
//        // אם המשתמש כבר מחובר, שלח אותו ישירות ל-MainActivity
//        if (firebaseAuth.currentUser != null) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }
//    }
//
//    // פונקציה להצגת הודעות Toast
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//}

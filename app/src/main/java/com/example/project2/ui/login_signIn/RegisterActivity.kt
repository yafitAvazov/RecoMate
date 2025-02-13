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
//class RegisterActivity : AppCompatActivity() {
//
//    private lateinit var username: EditText
//    private lateinit var password: EditText
//    private lateinit var confirmPassword: EditText
//    private lateinit var registerButton: Button
//    private lateinit var loginButton: Button
//    private lateinit var firebaseAuth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.register)
//
//        // קישור ה-XML לקוד
//        username = findViewById(R.id.username)
//        password = findViewById(R.id.password)
//        confirmPassword = findViewById(R.id.confirm_password)
//        registerButton = findViewById(R.id.Sign_Up_btn)
//        loginButton = findViewById(R.id.login_btn)
//
//        firebaseAuth = FirebaseAuth.getInstance()
//
//        // מאזין לכפתור ההרשמה
//        registerButton.setOnClickListener {
//            val userEmail = username.text.toString().trim()
//            val userPassword = password.text.toString().trim()
//            val userConfirmPassword = confirmPassword.text.toString().trim()
//
//            // בדיקות קלט
//            if (TextUtils.isEmpty(userEmail)) {
//                showToast("Please enter your email")
//                return@setOnClickListener
//            }
//            if (TextUtils.isEmpty(userPassword)) {
//                showToast("Please enter your password")
//                return@setOnClickListener
//            }
//            if (userPassword.length < 6) {
//                showToast("Password must be at least 6 characters")
//                return@setOnClickListener
//            }
//            if (userPassword != userConfirmPassword) {
//                showToast("Passwords do not match")
//                return@setOnClickListener
//            }
//
//            // הרשמה ב-Firebase
//            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        showToast("Registration successful!")
//                        startActivity(Intent(this, MainActivity::class.java))
//                        finish()
//                    } else {
//                        showToast("Registration failed: ${task.exception?.message}")
//                    }
//                }
//        }
//
//        // מאזין לכפתור המעבר למסך התחברות
//        loginButton.setOnClickListener {
//            startActivity(Intent(this, LoginActivity::class.java))
//        }
//
//        // אם המשתמש כבר מחובר, שלח אותו ל- MainActivity
//        if (firebaseAuth.currentUser != null) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }
//    }
//
//    // פונקציה להצגת Toast קצר
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//}

package com.example.project2.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.project2.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // קישור ה-BottomNavigationView ל-NavController
        bottomNavigationView.setupWithNavController(navController)

        // מאזין לשינויים בניווט ומסתיר את ה-BottomNavigationView אם המשתמש ב-StartFragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.visibility = if (destination.id == R.id.startFragment) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }
        }

        // מאזין ללחיצות בתפריט הניווט
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_all_recommendation -> {
                    navController.navigate(R.id.allItemsFragment)
                    true
                }
                R.id.nav_categories -> {
                   navController.navigate(R.id.categoriesFragment)
                   true
              }
//                R.id.nav_chat -> {
//                    navController.navigate(R.id.chatFragment)
//                    true
//                }
//                R.id.nav_profile -> {
//                    navController.navigate(R.id.profileFragment)
//                    true
//                }
//                R.id.nav_favorites -> {
//                    navController.navigate(R.id.favoritesFragment)
//                    true
//                }
                else -> false
            }
        }

        // הגדרת שולי מערכת להפחתת בעיות תצוגה
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

package com.example.project2.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.project2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) // ✅ כעת אין התנגשויות
        supportActionBar?.setDisplayShowTitleEnabled(false) // ✅ מסיר את הכותרת של ה-Toolbar ומאפשר לוגו

        val content = findViewById<View>(android.R.id.content)
        content.post {
            enableEdgeToEdge(window)
        }




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
                R.id.add_idem -> {
                    navController.navigate(R.id.addItemFragment)
                    true
                }
                R.id.nav_my_recommendations -> {  // 🔥 עמוד ההמלצות שלי
                    navController.navigate(R.id.myRecommendationsFragment)
                    true
                }
                R.id.nav_favorites -> {  // 🔥 עמוד ההמלצות שלי
                    navController.navigate(R.id.favoritesFragment)
                    true
                }
                else -> false

            }
        }

        // התאמה לשולי המערכת כדי למנוע חפיפה עם כפתורי הניווט
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom) // מרים את ה-BottomNavigationView מעל הכפתורים
            insets
        }
    }

    private fun enableEdgeToEdge(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }
}

package com.example.project2.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.project2.R

class FullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // הטענת הפריסה
        setContentView(R.layout.full_image_view)

        // קבלת ה-imageUri מה-Intent
        val imageUri = intent.getStringExtra("imageUri")

        // התחברות ל-ImageView
        val fullImageView = findViewById<ImageView>(R.id.full_image_view)
        if (!imageUri.isNullOrEmpty()) {
            Log.d("FullImageActivity", "imageUri: $imageUri")
            Glide.with(this)
                .load(Uri.parse(imageUri))
                .into(fullImageView)
        } else {
            // טיפול במקרה של imageUri ריק


        // טעינת התמונה
        if (!imageUri.isNullOrEmpty()) {
            // הוספת לוג לבדיקת הערך של imageUri
            Log.d("FullImageActivity", "imageUri: $imageUri")

            fullImageView.setImageURI(Uri.parse(imageUri))
        } else {
            Toast.makeText(this, "אין תמונה להצגה", Toast.LENGTH_SHORT).show()
            finish()
        }
            }
        // כפתור סגירה
        val closeButton = findViewById<ImageButton>(R.id.close_button)
        closeButton.setOnClickListener {
            finish()
        }
    }
}

package com.example.fusion_bank

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class RegisterActivity : AppCompatActivity() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNama = findViewById<EditText>(R.id.etNama)
        val email = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val nama = etNama.text.toString()
            val email = email.text.toString()
            val password = etPassword.text.toString()
            val noRek = (1..8).map { (0..9).random() }.joinToString("")

            db.collection("user").document(noRek).set(
                hashMapOf(
                    "username" to nama,
                    "email" to email,
                    "password" to password,
                    "saldo" to 0
                )
            ).addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("Register Success")
                    .setPositiveButton("OK") { dialog, which ->
                        finish()
                    }
                    .show()


            }
        }


    }
}
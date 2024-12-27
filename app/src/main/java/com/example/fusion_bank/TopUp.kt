package com.example.fusion_bank

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.NumberFormat
import java.util.Locale

class TopUp : AppCompatActivity() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_top_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val buttonTopUp = findViewById<Button>(R.id.topUpButton)

        buttonTopUp.setOnClickListener {
            val topUpAmount = findViewById<EditText>(R.id.topUpAmount).text.toString()

            if (topUpAmount == "" || topUpAmount.toInt() <= 0) {
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Invalid amount")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formattedAmount = currencyFormat.format(topUpAmount.toInt()).replace("Rp", "Rp ")

            AlertDialog.Builder(this)
                .setTitle("Confirm Top Up")
                .setMessage("Are you sure you want to top up $formattedAmount?")
                .setPositiveButton("Yes") { _, _ ->
                    topUp(topUpAmount.toInt())
                }
                .setNegativeButton("No", null)
                .show()
        }

    }

    fun topUp(amount: Int) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedAmount = currencyFormat.format(amount).replace("Rp", "Rp ")

        db.collection("user").document(noRek).get()
            .addOnSuccessListener { result ->
                // Ensure the saldo exists and is an integer
                val saldo = result.getLong("saldo")?.toInt() ?: 0
                val username = result.getString("username") ?: "Unknown User" // Fetch username

                // Update the saldo
                db.collection("user").document(noRek)
                    .update("saldo", saldo + amount)
                    .addOnSuccessListener {
                        reset() // Reset and notify user of success
                    }
                    .addOnFailureListener { e ->
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Failed to update balance: ${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }

                // Add transaction after saldo update
                db.collection("transaksi")
                    .add(
                        Transaksi(
                            sender = username, // Use the retrieved username
                            receiver = username, // Use the same username since it's a top-up
                            jumlah = amount,
                            berita = "Top Up $formattedAmount",
                            tanggal = com.google.firebase.Timestamp.now()
                        )
                    )
                    .addOnFailureListener { e ->
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Failed to log transaction: ${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
            }
            .addOnFailureListener { e ->
                // Handle document fetch failure
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Failed to fetch user data: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
    }


    fun reset() {
        AlertDialog.Builder(this)
            .setTitle("Top Up Successful")
            .setMessage("Your balance has been updated")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish() // Ensures all activities are removed
            }
            .show()
    }

    companion object {
        var noRek: String = ""
    }
}
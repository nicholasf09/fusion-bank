package com.example.fusion_bank

import android.content.DialogInterface
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

        db.collection("user").document(noRek).get()
            .addOnSuccessListener { result ->
                val saldo = result["saldo"].toString().toInt()
                db.collection("user").document(noRek).update("saldo", saldo + amount)
                reset()
            }
    }


    fun reset() {
        AlertDialog.Builder(this)
            .setTitle("Top Up Successful")
            .setMessage("Your balance has been updated")
            .setPositiveButton("OK", null)
            .show()

        findViewById<EditText>(R.id.topUpAmount).text.clear()
    }

    companion object {
        val noRek = "0907 2004"
    }
}
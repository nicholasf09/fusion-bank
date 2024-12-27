package com.example.fusion_bank

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class TransferActivity : AppCompatActivity() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transfer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnTransfer = findViewById<Button>(R.id.btnTransfer)
        val btnScanner = findViewById<Button>(R.id.btnScanner)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        var etRekening = findViewById<EditText>(R.id.etRekening)
        var etBerita = findViewById<EditText>(R.id.etBerita)
        var etNominal = findViewById<EditText>(R.id.etNominal)

        btnTransfer.setOnClickListener {
            val receiverRekening = etRekening.text.toString()
            val nominal = etNominal.text.toString().toIntOrNull()

            if (receiverRekening.isBlank() || nominal == null || nominal <= 0) {
                Log.d("TransferActivity", "Invalid input")
                return@setOnClickListener
            }

            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Confirm Transaction")
                .setMessage(
                    "Are you sure you want to send Rp$nominal to account number $receiverRekening?"
                )
                .setPositiveButton("Yes") { _, _ ->
                    // Call createTransaksi if confirmed
                    createTransaksi(
                        db = db,
                        senderNoRek = noRek,
                        receiverNoRek = receiverRekening,
                        jumlah = nominal,
                        berita = etBerita.text.toString(),
                        tanggal = Timestamp.now()
                    )

                    // Reset the app after successful transaction
                    reset()
                }
                .setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog and stay on the page
                    dialog.dismiss()
                }
                .show()
        }

        btnScanner.setOnClickListener {
            val intent = Intent(this, QRCodeScanner::class.java)
            startActivity(intent)
        }

        val scanResult = intent.getStringExtra("SCAN_RESULT")

        if (scanResult != null) {
            etRekening.setText(scanResult)
        }

        btnGenerate.setOnClickListener {
            val intent = Intent(this, GenerateQR::class.java)
            intent.putExtra("NO_REK", noRek)
            startActivity(intent)
        }
    }

    companion object {
        var noRek: String = ""
    }

    fun createTransaksi(
        db: FirebaseFirestore,
        senderNoRek: String,
        receiverNoRek: String,
        jumlah: Number,
        berita: String,
        tanggal: Timestamp
    ) {
        // Perform a Firestore transaction
        db.runTransaction { transaction ->
            // Fetch sender data
            val senderDocRef = db.collection("user").document(senderNoRek)
            val senderSnapshot = transaction.get(senderDocRef)
            val senderSaldo = senderSnapshot.getLong("saldo") ?: 0
            val senderUsername = senderSnapshot.getString("username") ?: "Unknown Sender"

            // Check if sender has enough saldo
            if (senderSaldo < jumlah.toLong()) {
                throw Exception("Insufficient saldo for sender")
            }

            // Fetch receiver data
            val receiverDocRef = db.collection("user").document(receiverNoRek)
            val receiverSnapshot = transaction.get(receiverDocRef)
            val receiverSaldo = receiverSnapshot.getLong("saldo") ?: 0
            val receiverUsername = receiverSnapshot.getString("username") ?: "Unknown Receiver"

            // Update saldo for sender and receiver
            transaction.update(senderDocRef, "saldo", senderSaldo - jumlah.toLong())
            transaction.update(receiverDocRef, "saldo", receiverSaldo + jumlah.toLong())

            // Create a new transaction record
            val transaksi = Transaksi(
                sender = senderUsername,
                receiver = receiverUsername,
                jumlah = jumlah,
                berita = berita,
                tanggal = tanggal
            )
            db.collection("transaksi").add(transaksi)

            // Transaction succeeded
            null
        }.addOnSuccessListener {
            Log.d("Firebase", "Transaction completed successfully")
        }.addOnFailureListener { e ->
            Log.d("Firebase", "Transaction failed: ${e.message}")
        }
    }

    fun reset() {
        AlertDialog.Builder(this)
            .setTitle("Transaction Successful")
            .setMessage("Your transaction has been completed")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finishAffinity() // Ensures all activities are removed
            }
            .show()
    }
}
package com.example.fusion_bank

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    private var transaksi = ArrayList<Transaksi>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val username = findViewById<TextView>(R.id.username)
        val noRek = findViewById<TextView>(R.id.norek)
        val saldo = findViewById<TextView>(R.id.saldo)
        val _btnHistory = findViewById<ImageButton>(R.id.btnHistory)
        val _btnTopUp = findViewById<ImageButton>(R.id.btnTopUp)
        val _btnTransfer = findViewById<ImageButton>(R.id.btnTransfer)
        val signOut = findViewById<LinearLayout>(R.id.signOut)
        recyclerView = findViewById(R.id.recyclerView)

        // Sign Out Button
        signOut.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            email = ""
            rekening = ""
            startActivity(intent)
        }

        // History Button
        _btnHistory.setOnClickListener {
            val intent = Intent(this, Mutasi::class.java)
            Mutasi.noRek = noRek.text.toString()
            Mutasi.nama = username.text.toString()
            startActivity(intent)
        }

        // Top-Up Button
        _btnTopUp.setOnClickListener {
            val intent = Intent(this, TopUp::class.java)
            TopUp.noRek = noRek.text.toString()
            startActivity(intent)
        }

        // Transfer Button
        _btnTransfer.setOnClickListener {
            val intent = Intent(this, TransferActivity::class.java)
            TransferActivity.noRek = noRek.text.toString()
            startActivity(intent)
        }

        // Load User and Transaction Data
        db.collection("user")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = User(
                        document.id,
                        document.data["username"].toString(),
                        document.data["saldo"].toString(),
                        document.data["email"].toString(),
                        document.data["password"].toString()
                    )
                    noRek.text = user.noRek
                    username.text = user.username

                    val formattedSaldo = formatCurrency(user.saldo)
                    saldo.text = formattedSaldo
                }

                // Load the last 3 transactions
                CoroutineScope(Dispatchers.Main).launch {
                    fetchLastThreeTransactions(username.text.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents: ", exception)
            }
    }

    private suspend fun fetchLastThreeTransactions(username: String) {
        try {
            // Fetch transactions where the user is either sender or receiver
            val resultReceiver = db.collection("transaksi")
                .whereEqualTo("receiver", username)
                .get()
                .await()

            for (document in resultReceiver) {
                val transaksiItem1 = Transaksi(
                    document.data["sender"].toString(),
                    document.data["receiver"].toString(),
                    document.data["jumlah"].toString().toInt(),
                    document.data["berita"].toString(),
                    document.data["tanggal"] as Timestamp
                )
                if (!transaksi.contains(transaksiItem1)) {
                    transaksiItem1.receiver = transaksiItem1.sender
                    transaksi.add(transaksiItem1)
                }
            }

            val resultSender = db.collection("transaksi")
                .whereEqualTo("sender", username)
                .get()
                .await()

            for (document in resultSender) {
                val transaksiItem2 = Transaksi(
                    document.data["sender"].toString(),
                    document.data["receiver"].toString(),
                    document.data["jumlah"].toString().toInt(),
                    document.data["berita"].toString(),
                    document.data["tanggal"] as Timestamp
                )
                if (!transaksi.contains(transaksiItem2)) {
                    transaksiItem2.jumlah = transaksiItem2.jumlah.toInt() * -1
                    transaksi.add(transaksiItem2)
                }
            }

            // Sort by date and take the last 3 transactions
            transaksi.sortByDescending { it.tanggal }
            val lastThreeTransactions = transaksi.take(3)

            // Bind the filtered list to the RecyclerView
            showTransactions(lastThreeTransactions)
        } catch (e: Exception) {
            Log.e("TAG", "Error fetching transactions: ${e.message}")
        }
    }

    private fun showTransactions(transactions: List<Transaksi>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapterMutasi(transactions)
    }

    private fun formatCurrency(amount: String): String {
        var tempNumber = amount
        val sisa = tempNumber.length % 3
        var rupiah = if (sisa == 0) "" else tempNumber.substring(0, sisa)
        val ribuan = tempNumber.substring(sisa).chunked(3)

        if (ribuan != null) {
            val separator = if (sisa != 0) "." else ""
            rupiah += separator + ribuan.joinToString(".")
        }
        return "Rp. $rupiah"
    }

    companion object {
        var email: String = ""
        var rekening: String = ""
    }
}
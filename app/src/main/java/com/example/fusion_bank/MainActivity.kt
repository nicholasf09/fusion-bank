package com.example.fusion_bank

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    var db = Firebase.firestore

    var transaksi = ArrayList<Transaksi>()
    var user = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var username = findViewById<TextView>(R.id.username)
        var noRek = findViewById<TextView>(R.id.norek)
        var saldo = findViewById<TextView>(R.id.saldo)

        db.collection("user")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var user = User(
                        document.id,
                        document.data["username"].toString(),
                        document.data["saldo"].toString(),
                        document.data["email"].toString(),
                        document.data["password"].toString()
                    )
                    noRek.text = user.noRek
                    username.text = user.username

                    var tempNumber = user.saldo
                    var sisa = tempNumber.length % 3 // cek apakah kelipatan 3
                    var rupiah = if (sisa == 0) "" else tempNumber.substring(0, sisa) // jika iya maka ambil angka didepan
                    val ribuan = tempNumber.substring(sisa).chunked(3) // kemudian ambil 3 angka berikutnya

                    if (ribuan != null) {
                        val separator = if (sisa != 0) "." else ""
                        rupiah += separator + ribuan.joinToString(".")
                    }

                    var finalSaldo = "Rp. $rupiah"
                    saldo.text = finalSaldo
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents: ", exception)
            }





    }
    companion object {
        var email: String = ""
    }
}
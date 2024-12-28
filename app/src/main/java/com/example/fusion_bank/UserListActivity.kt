package com.example.fusion_bank

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fusion_bank.MainActivity.Companion.email
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserListActivity : AppCompatActivity() {

    var db = Firebase.firestore
    lateinit var lvAdapter : SimpleAdapter
    var DataUser = ArrayList<User>()
    var data : MutableList<Map<String, String>> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val _lvUser = findViewById<ListView>(R.id.username)

        var email = intent.getStringExtra("email")

        var rekening = intent.getStringExtra("rekening").toString()

        readData(db)

        lvAdapter = SimpleAdapter(
            this,
            data,
            android.R.layout.simple_list_item_2,
            arrayOf<String>("Nama", "Hasil"),
            intArrayOf(
                android.R.id.text1,
                android.R.id.text2
            )
        )
        _lvUser.adapter = lvAdapter

        _lvUser.setOnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position) as Map<String, String>
            val intent = Intent(this, TransferActivity::class.java)
            intent.putExtra("receiver", item["Nama"])
            TransferActivity.noRek = rekening
            startActivity(intent)
            Log.d("UserListActivity", "Receiver: ${item["Nama"]}, Norek: ${TransferActivity.noRek}")
        }

    }
    fun readData(db: FirebaseFirestore) {
        // Filter data with Firestore query where "nohp" matches StoredActivity.nohp
        db.collection("user")
            .whereNotEqualTo("email", email) // Filter condition
            .get()
            .addOnSuccessListener { result ->
                DataUser.clear()
                data.clear()
                for (document in result) {
                    val readData = User(
                        document.id,
                        document.data["username"].toString(),
                        document.data["saldo"].toString(),
                        document.data["email"].toString(),
                        document.data["password"].toString()
                    )
                    DataUser.add(readData)

                    val dt: MutableMap<String, String> = HashMap(2)
                    dt["Nama"] = document.id
                    dt["Hasil"] = document.data["username"].toString()
                    data.add(dt)
                }
                lvAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("Firebase", it.message.toString())
            }
    }
}
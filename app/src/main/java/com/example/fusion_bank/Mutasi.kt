package com.example.fusion_bank

import android.os.Bundle
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Mutasi : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var arTransaksi = ArrayList<Transaksi>()
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mutasi)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)

        findViewById<TextView>(R.id.noRek).text = noRek
        findViewById<TextView>(R.id.nama).text = nama

        CoroutineScope(Dispatchers.Main).launch {
            readData()
            initHome()

            val filteredList = arTransaksi.filter {
                val calendar: Calendar = Calendar.getInstance()
                calendar.time = it.tanggal.toDate()

                val last: Calendar = Calendar.getInstance()
                last.time = arTransaksi[arTransaksi.size - 1].tanggal.toDate()

                calendar.get(Calendar.MONTH) == last.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == last.get(Calendar.YEAR)
            }

            showData(filteredList as ArrayList<Transaksi>)

            val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView)
            scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }

    }

    suspend fun readData() {
        val result = db.collection("transaksi").get().await()
        for (document in result) {
            arTransaksi.add(
                Transaksi(
                    document.data["sender"].toString(),
                    document.data["receiver"].toString(),
                    document.data["jumlah"].toString().toInt(),
                    document.data["berita"].toString(),
                    document.data["tanggal"] as Timestamp
                )
            )
        }
    }

    fun initHome() {
        arTransaksi.sortBy { it.tanggal }
        val listBulan = findViewById<LinearLayout>(R.id.bulan)
        var selectedTextView: TextView? = null

        for (transaksi in arTransaksi) {
            val tanggalTransaksi = transaksi.tanggal
            val calendar: Calendar = Calendar.getInstance()
            calendar.time = tanggalTransaksi.toDate()

            val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)

            val bulanId = (calendar.get(Calendar.MONTH).toString() +  calendar.get(Calendar.YEAR)).toInt()
            val textView = TextView(this).apply {
                text = "$monthName " + calendar.get(Calendar.YEAR)
                id = bulanId
                textSize = 16.0F
                setPadding(12, 12, 12, 64)
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 32, 0)
            }
            textView.layoutParams = layoutParams

            if (listBulan.childCount <= 0 || findViewById<TextView>(bulanId) == null) {
                listBulan.addView(textView)

                findViewById<TextView>(bulanId).setOnClickListener {
                    selectedTextView?.background = null
                    textView.background = ContextCompat.getDrawable(this, R.drawable.underline)
                    textView.setPadding(12, 12, 12, 64)

                    selectedTextView = textView
                    getByMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
                }

            }

        }
        listBulan.get(listBulan.childCount - 1).performClick()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun showData(list: ArrayList<Transaksi>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapterMutasi(list)
    }

    fun getByMonth(bulan: Int, tahun: Int) {
        val filteredList = arTransaksi.filter {
            val calendar: Calendar = Calendar.getInstance()
            calendar.time = it.tanggal.toDate()
            calendar.get(Calendar.MONTH) == bulan && calendar.get(Calendar.YEAR) == tahun
        }

        showData(filteredList as ArrayList<Transaksi>)
    }

    companion object {
        val noRek = "0907 2004"
        val nama = "Test"
    }

}
package com.example.fusion_bank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class adapterMutasi (private val data: List<Transaksi>) : RecyclerView.Adapter<adapterMutasi.ListViewHolder>() {
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tanggal = itemView.findViewById<TextView>(R.id.tanggal)
        var jmlhMutasi = itemView.findViewById<TextView>(R.id.jmlhMutasi)
        var rekTujuan = itemView.findViewById<TextView>(R.id.rekTujuan)
        var berita = itemView.findViewById<TextView>(R.id.berita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.mutasi_recycler, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.itemView.setBackgroundResource(R.drawable.background_transaksi)
        holder.itemView.elevation = 8.0f

        var transaksi = data[position]

        val jumlahMutasi = transaksi.jumlah.toInt()

        val green = ContextCompat.getColor(holder.itemView.context, R.color.blue)
        val red = ContextCompat.getColor(holder.itemView.context, R.color.red)

        if (jumlahMutasi < 0) {
            holder.jmlhMutasi.setTextColor(red)
        } else {
            holder.jmlhMutasi.setTextColor(green)
        }

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedAmount = currencyFormat.format(jumlahMutasi).replace("Rp", "Rp ")
        holder.jmlhMutasi.text = formattedAmount

        val calendar: Calendar = Calendar.getInstance()
        calendar.time = transaksi.tanggal.toDate()
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)

        holder.tanggal.text = (calendar.get(Calendar.DAY_OF_MONTH).toString() + " $monthName " + calendar.get(Calendar.YEAR))
        holder.rekTujuan.text = transaksi.receiverName
        holder.berita.text = transaksi.berita

    }

}
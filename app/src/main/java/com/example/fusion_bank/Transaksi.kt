package com.example.fusion_bank

import com.google.firebase.Timestamp

data class Transaksi(
    var sender: String,
    var receiverName: String,
    var jumlah: Number,
    var berita: String,
    var tanggal: Timestamp
)

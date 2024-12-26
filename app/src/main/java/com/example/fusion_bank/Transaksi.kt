package com.example.fusion_bank

data class Transaksi(
    var id: String,
    var sender: String,
    var receiverName: String,
    var jumlah: Number,
    var berita: String,
    var tanggal: String
)

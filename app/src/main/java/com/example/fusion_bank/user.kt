package com.example.fusion_bank

data class user(
    var noRek: String,
    var username: String,
    var saldo: Double,
    var email: String,
    var password: String,
    var transaksi: List<mutasi> = listOf()
)

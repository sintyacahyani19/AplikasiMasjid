package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mosque_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val isIncome: Boolean, // true = Pemasukan, false = Pengeluaran
    val category: String,  // e.g. "Kotak Amal", "Zakat", "Operasional", etc.
    val dateMillis: Long = System.currentTimeMillis()
)

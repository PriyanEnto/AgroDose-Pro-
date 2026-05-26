package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val mode: String, // CalculationMode name
    val timestamp: Long = System.currentTimeMillis(),
    val inputJson: String,
    val outputJson: String
)

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["level"]),
        Index(value = ["feature"])
    ]
)
data class AppLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // "INFO", "WARN", "ERROR"
    val feature: String, // "Auth", "Gemini", "Meta", "Database", "Publishing", "Network", "AppCheck"
    val operation: String,
    val message: String
)

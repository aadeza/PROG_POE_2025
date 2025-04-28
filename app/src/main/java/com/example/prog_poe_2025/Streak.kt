package com.example.prog_poe_2025

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Streak")
data class Streak(
    @PrimaryKey val Id: Int = 1,
    val currentStreak: Int,
    val lastLoggedDate: String
)

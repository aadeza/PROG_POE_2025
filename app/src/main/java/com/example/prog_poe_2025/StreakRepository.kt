package com.example.prog_poe_2025

import DAOs.streakDAO
import java.time.LocalDate

class StreakRepository(private val StreakDao: streakDAO) {

    suspend fun updateStreakAfterLogging(){
        val today = LocalDate.now()
        val currentStreak = StreakDao.getStreak()

        if(currentStreak == null){
            StreakDao.insertStreak(Streak(currentStreak = 1, lastLoggedDate = today.toString()))

        }else{
            val lastDate = LocalDate.parse(currentStreak.lastLoggedDate)
            when{
                lastDate == today -> {

                }
                lastDate.plusDays(1) == today -> {
                    val newStreak = currentStreak.currentStreak + 1
                    StreakDao.insertStreak(Streak(Id = 1, currentStreak = newStreak, lastLoggedDate = today.toString()))
                }
                else -> {
                    StreakDao.insertStreak(Streak(Id = 1, currentStreak = 1, lastLoggedDate = today.toString()))
                }
            }
        }
    }

    suspend fun getStreakCount(): Int{
        return StreakDao.getStreak()?.currentStreak ?: 0
    }
}
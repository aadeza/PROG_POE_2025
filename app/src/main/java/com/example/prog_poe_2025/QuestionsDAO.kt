package com.example.prog_poe_2025

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy

@Dao
interface QuestionsDAO {

    @Query("SELECT * FROM Questions")
    suspend fun getAllQuestions() : List<Questions>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions : List<Questions>)

}
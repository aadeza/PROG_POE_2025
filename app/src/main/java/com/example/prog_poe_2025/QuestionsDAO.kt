package com.example.prog_poe_2025

import Data_Classes.Questions
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy

@Dao
interface QuestionsDAO {

    @Query("SELECT * FROM Questions ORDER BY RANDOM() LIMIT 10")
    suspend fun getAllQuestions() : List<Questions>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions : List<Questions>)

}
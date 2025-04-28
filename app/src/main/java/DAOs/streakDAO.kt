package DAOs

import com.example.prog_poe_2025.Streak
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface streakDAO {

    @Query("SELECT * FROM Streak WHERE Id = 1")
    suspend fun getStreak(): Streak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: Streak)

    @Update
    suspend fun updateStreak(streak: Streak)

    @Query("DELETE FROM Streak")
    suspend fun deleteStreak()
}
package DAOs

import Data_Classes.Income
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDAO {

    @Insert
    suspend fun insertIncome(income: Income)

    @Query("SELECT * FROM Income WHERE user_id = :userId")
    suspend fun getIncomeByUser(userId: Int): List<Income>

    @Query("DELETE FROM Income")
    suspend fun deleteAllIncome()

    @Query("SELECT * FROM Income ORDER BY date LIMIT :limit")
    suspend fun getLatestIncomes(limit: Int): List<Income>


    @Query("SELECT * FROM Income WHERE user_id = :userId ORDER BY date DESC")
    suspend fun getAllIncomes(userId: Int): List<Income>


    @Query("SELECT SUM(amount) FROM Income WHERE user_id = :userId")
    suspend fun getTotalIncome(userId: Int): Long?





    @Query("SELECT SUM(amount) FROM Income WHERE user_id = :userId AND category = :category AND date >= :startTime")
    suspend fun getTotalSpentInCategory(userId: Int, category: String, startTime: Long): Float?

}

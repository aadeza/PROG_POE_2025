package DAOs

import Data_Classes.Income
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IncomeDAO {

    @Insert
    suspend fun insertIncome(income: Income)

    @Query("SELECT * FROM Income WHERE user_id = :userId")
    suspend fun getIncomeByUser(userId: Int): List<Income>

    @Query("DELETE FROM Income")
    suspend fun deleteAllIncome()


    @Query("SELECT SUM(amount) FROM Income WHERE user_id = :userId AND category = :category AND date >= :startTime")
    suspend fun getTotalSpentInCategory(userId: Int, category: String, startTime: Long): Float?
}

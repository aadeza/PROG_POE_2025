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

    @Query("SELECT COALESCE(SUM(amount), 0) FROM Income WHERE user_id = :userId AND category = :category AND budgetId = :budgetId AND date >= :startTime")
    suspend fun getTotalIncomeInCategory(userId: Int, category: String, budgetId: Int, startTime: Long): Float

    @Query("SELECT * FROM Income")
    fun getAllIncome(): List<Income>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM Income WHERE user_id = :userId")
    suspend fun getTotalIncome(userId: Int): Double

    @Query("SELECT * FROM Income WHERE user_id = :userId ORDER BY date DESC LIMIT 3")
    suspend fun getLatestIncomes(userId: Int): List<Income>

}
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
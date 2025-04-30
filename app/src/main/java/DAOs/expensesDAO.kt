package DAOs

import Data_Classes.Expenses
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpensesDAO {

    @Insert
    suspend fun insertExpense(expense: Expenses)

    @Query("SELECT * FROM Expenses WHERE user_id = :userId")
    suspend fun getExpensesByUser(userId: Int): List<Expenses>

    @Query("DELETE FROM Expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM Expenses ORDER BY date LIMIT :limit")
    suspend fun getLatestExpenses(limit: Int): List<Expenses>



    @Query("SELECT * FROM Expenses WHERE user_id = :userId ORDER BY date DESC")
    suspend fun getAllExpenses(userId: Int): List<Expenses>


    @Query("SELECT SUM(amount) FROM Expenses WHERE user_id = :userId")
    suspend fun getTotalExpenses(userId: Int): Long?


    @Query("SELECT SUM(amount) FROM Expenses WHERE user_id = :userId AND category = :category AND date >= :startTime")
    suspend fun getTotalSpentInCategory(userId: Int, category: String, startTime: Long): Float?
}


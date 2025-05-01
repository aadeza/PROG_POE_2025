package DAOs

import Data_Classes.Expenses
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpensesDAO {

    @Insert
    suspend fun insertExpense(expense: Expenses)

    @Query("SELECT * FROM Expenses WHERE user_id = :userId")
    suspend fun getExpensesByUser(userId: Int): List<Expenses>

    @Query("DELETE FROM Expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT SUM(amount) FROM Expenses WHERE user_id = :userId AND category = :category AND budgetId = :budgetId AND date >= :startTime")
    suspend fun getTotalSpentInCategory(userId: Int, category: String, budgetId: Int, startTime: Long): Float?

    @Query("UPDATE Expenses SET amount = :newAmount WHERE user_id = :userId AND category = :category AND budgetId = :budgetId")
    suspend fun updateExpenseAmount(userId: Int, category: String, newAmount: Float, budgetId: Int)

    @Query("SELECT * FROM Expenses")
    fun getAllExpenses(): List<Expenses> // ✅ This fetches all expense transactions
}


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

    @Query("SELECT * FROM Expenses ORDER BY date LIMIT :limit")
    suspend fun getLatestExpenses(limit: Int): List<Expenses>

}


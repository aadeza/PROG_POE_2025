package DAOs

import Data_Classes.Budgets
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface BudgetDAO {

    @Insert
    suspend fun insertBudget(budget: Budgets)

    @Query("SELECT * FROM Budgets WHERE user_id = :userId")
    suspend fun getBudgetsForUser(userId: String): List<Budgets>

    @Update
    suspend fun updateBudget(budget: Budgets)

    @Delete
    suspend fun deleteBudget(budget: Budgets)

    @Query("DELETE FROM Budgets")
    suspend fun deleteAllBudgets()
}

package DAOs

import Data_Classes.BudgetCategoryCrossRef
import Data_Classes.BudgetWithCategories
import Data_Classes.Budgets
import Data_Classes.Category
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface BudgetDAO {

    @Insert
    suspend fun insertBudget(budget: Budgets): Long

    @Query("SELECT * FROM Budgets WHERE user_id = :userId")
    suspend fun getBudgetsForUser(userId: Int): List<Budgets>

    @Update
    suspend fun updateBudget(budget: Budgets)

    @Query("UPDATE Budgets SET maxMonthGoal = :maxGoal WHERE id = :budgetId")
    suspend fun updateMaxGoal(budgetId: Int, maxGoal: Long)

    @Delete
    suspend fun deleteBudget(budget: Budgets)

    @Query("DELETE FROM Budgets")
    suspend fun deleteAllBudgets()

    @Insert
    suspend fun insertBudgetCategoryCrossRefs(crossRefs: List<BudgetCategoryCrossRef>)

    @Transaction
    @Query("SELECT * FROM Budgets WHERE id = :budgetId")
    suspend fun getBudgetWithCategories(budgetId: Int): BudgetWithCategories

    @Query("SELECT * FROM Budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Int): Budgets?

    @Transaction
    @Query("SELECT * FROM Category WHERE id IN (SELECT categoryId FROM BudgetCategoryCrossRef WHERE budgetId = :budgetId)")
    suspend fun getCategoriesForBudget(budgetId: Int): List<Category>

    @Query("DELETE FROM BudgetCategoryCrossRef WHERE budgetId = :budgetId")
    suspend fun deleteBudgetCategoryCrossRefsForBudget(budgetId: Int)

    @Query("DELETE FROM BudgetCategoryCrossRef WHERE budgetId = :budgetId")
    suspend fun deleteBudgetCategories(budgetId: Int)

    @Query("DELETE FROM Budgets WHERE id = :budgetId")
    suspend fun deleteBudgetById(budgetId: Int)

    @Insert
    suspend fun insertReport(budget: Budgets)

    @Query("SELECT MAX(amount) FROM Expenses WHERE budgetId = :budgetId")
    suspend fun getHighestExpense(budgetId: Int): Long?

    @Query("SELECT MAX(amount) FROM Income WHERE budgetId = :budgetId")
    suspend fun getHighestIncome(budgetId: Int): Long?

    @Query("SELECT * FROM Budgets WHERE id IN (SELECT budgetId FROM BudgetCategoryCrossRef WHERE categoryId IN (SELECT id FROM Category WHERE name = :category))")
    suspend fun getBudgetsForCategory(category: String): List<Budgets>
}
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/

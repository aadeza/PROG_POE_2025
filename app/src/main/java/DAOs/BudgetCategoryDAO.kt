package DAOs

import Data_Classes.BudgetCategoryCrossRef
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import Data_Classes.BudgetWithCategories
import Data_Classes.CategoryWithBudgets

@Dao
interface BudgetCategoryDAO {

    @Insert
    suspend fun insertCrossRef(crossRef: BudgetCategoryCrossRef)

    @Query("DELETE FROM BudgetCategoryCrossRef")
    suspend fun deleteAllCrossRefs()

    @Transaction
    @Query("SELECT * FROM Budgets WHERE id = :budgetId")
    suspend fun getBudgetWithCategories(budgetId: Int): BudgetWithCategories

    @Transaction
    @Query("SELECT * FROM Category WHERE id = :categoryId")
    suspend fun getCategoryWithBudgets(categoryId: Int): CategoryWithBudgets
}

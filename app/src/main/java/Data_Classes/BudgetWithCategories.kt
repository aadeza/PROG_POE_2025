package Data_Classes

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BudgetWithCategories(
    @Embedded val budget: Budgets,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BudgetCategoryCrossRef::class,
            parentColumn = "budgetId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<Category>
)
data class CategoryWithBudgets(
    @Embedded val category: Category,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BudgetCategoryCrossRef::class,
            parentColumn = "categoryId",
            entityColumn = "budgetId"
        )
    )
    val budgets: List<Budgets>
)

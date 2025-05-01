package Data_Classes

import androidx.room.Entity

@Entity(
    primaryKeys = ["budgetId", "categoryId"]
)
data class BudgetCategoryCrossRef(
    val budgetId: Int,
    val categoryId: Int
)
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
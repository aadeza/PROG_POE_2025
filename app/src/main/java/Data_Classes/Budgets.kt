package Data_Classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "Budgets",

    foreignKeys = [
        ForeignKey(
            entity = Users::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Budgets(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val budgetType: String,
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val minMonthGoal: Long,
    val maxMonthGoal: Long,
    val user_id: Int
)
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
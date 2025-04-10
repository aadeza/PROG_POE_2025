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
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val minMonthGoal: Long,
    val maxMonthGoal: Long,
    val TotalAmount: Long,
    val userid: String
)

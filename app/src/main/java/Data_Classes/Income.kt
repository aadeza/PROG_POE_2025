package Data_Classes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.prog_poe_2025.spTransaction

@Entity(
    tableName = "Income",
    foreignKeys = [
        ForeignKey(entity = Users::class, parentColumns = ["id"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Budgets::class, parentColumns = ["id"], childColumns = ["budgetId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    override val amount: Long,
    override val description: String? = null,
    override val category: String,
    override val date: Long,
    override val transaction_type: String,
    override val imagePath: String? = null,
    override val user_id: Int,
    override val budgetId: Int,
    override val isExpense: Boolean = false
) : spTransaction(amount, description, category, date, transaction_type, imagePath, user_id, budgetId, isExpense)

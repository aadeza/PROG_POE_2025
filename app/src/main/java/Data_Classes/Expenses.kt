package Data_Classes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Expenses",

    foreignKeys = [
        ForeignKey(
            entity = Users::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("userid"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Expenses(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Long,
    val description: String? = null,
    val category: String,
    val date: Long,
    val transaction_type: String,
    val imagePath: String? = null,
    val userid: String
)

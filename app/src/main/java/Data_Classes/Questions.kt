package Data_Classes

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Questions")
data class Questions(
@PrimaryKey(autoGenerate = true) val id : Int = 0,
    val text: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val correctAnswer: String

)

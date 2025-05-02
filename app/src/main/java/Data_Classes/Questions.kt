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
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
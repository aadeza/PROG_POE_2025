package Data_Classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(tableName = "Category")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    var selected: Boolean = false
)
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
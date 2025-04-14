package Data_Classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(tableName = "Category")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
) {
    @Ignore
    var selected: Boolean = false
}

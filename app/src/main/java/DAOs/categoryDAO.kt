package DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import Data_Classes.Category
import androidx.room.Ignore

@Dao
interface CategoryDAO {

    // Insert a single category
    @Insert
    suspend fun insertCategory(category: Category)

    // Insert multiple categories at once
    @Insert
    suspend fun insertAll(categories: List<Category>)

    // Query to get all categories as LiveData for observation
    @Query("SELECT * FROM Category")
    fun getAllCategoriesLive(): LiveData<List<Category>>  // This will now return LiveData

    // Query to delete all categories
    @Query("DELETE FROM Category")
    suspend fun deleteAllCategories()

    @Query("SELECT * FROM Category")
    suspend fun getAllCategories(): List<Category>

}
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
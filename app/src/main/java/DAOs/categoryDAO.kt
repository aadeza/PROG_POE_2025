package DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import Data_Classes.Category
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy

@Dao
interface CategoryDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long


    @Query("SELECT * FROM Category")
    suspend fun getAllCategories(): List<Category>

    @Query("DELETE FROM Category")
    suspend fun deleteAllCategories()
}

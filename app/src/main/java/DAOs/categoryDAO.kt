package DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import Data_Classes.Category

@Dao
interface CategoryDAO {

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM Category")
    suspend fun getAllCategories(): List<Category>

    @Query("DELETE FROM Category")
    suspend fun deleteAllCategories()
}

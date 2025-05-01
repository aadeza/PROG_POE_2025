package DAOs

import Data_Classes.Users
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: Users)

    @Query("DELETE FROM Users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM Users WHERE email = :email")
    suspend fun getUserByEmail(email: String): Users?

    // Added method to get user by ID
    @Query("SELECT * FROM Users WHERE id = :userId")
    suspend fun getUserById(userId: Int): Users?
}

//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
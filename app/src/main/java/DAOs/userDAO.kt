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



}

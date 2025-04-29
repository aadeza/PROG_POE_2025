package DAOs

import Data_Classes.Users
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: Users)

    @Query("DELETE FROM Users")
    suspend fun deleteAllUsers()


    @Query("SELECT * FROM Users WHERE email = :email")
    suspend fun getUserByEmail(email: String): Users?


    @Update
    suspend fun updateUser(user: Users)


}

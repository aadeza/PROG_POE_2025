package DAOs

import Data_Classes.Notification
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT * FROM notification ORDER BY timestamp DESC")
     fun getAllNotifications(): LiveData<List<Notification>>

    @Query("DELETE FROM notification")
    suspend fun clearNotifications()

    }





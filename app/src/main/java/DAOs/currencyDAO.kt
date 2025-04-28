package DAOs

import Data_Classes.Currency
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface currencyDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCurrencyPreference(currency: Currency)

    @Query("SELECT currencyCode FROM currency WHERE id = 1")
    suspend fun getCurrencyCode(): String?
}
package DAOs

import Data_Classes.QuizScores
import Data_Classes.LeaderboardEntry
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuizScoresDAO {

    @Insert
    suspend fun insertResult(result: QuizScores)

    @Query("SELECT * FROM QuizScores WHERE user_id = :userId")
    suspend fun getResultsByUser(userId: String): List<QuizScores>

    @Query("""
        SELECT user_id, AVG(scorePercentage) as averageScore, COUNT(*) as testCount
        FROM QuizScores
        GROUP BY user_id
        ORDER BY averageScore DESC, testCount DESC
    """)
    suspend fun getLeaderboard(): List<LeaderboardEntry>
}
//(Android Developers,2025)


/* References List
Android Developer, 2025. Save data in a local database using Room, 23 April 2025. [Online]. Available at:
https://developer.android.com/training/data-storage/room/ [ Accessed 20 April 2025].
*/
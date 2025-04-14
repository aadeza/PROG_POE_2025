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

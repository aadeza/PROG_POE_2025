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

    @Query("SELECT * FROM QuizScores WHERE userId = :userId")
    suspend fun getResultsByUser(userId: String): List<QuizScores>

    @Query("""
        SELECT userId, AVG(scorePercentage) as averageScore, COUNT(*) as testCount
        FROM QuizScores
        GROUP BY userId
        ORDER BY averageScore DESC, testCount DESC
    """)
    suspend fun getLeaderboard(): List<LeaderboardEntry>
}

package Data_Classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "QuizScores",
    foreignKeys = [ForeignKey(
        entity = Users::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class QuizScores(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val scorePercentage: Float,
    val timestamp: Long
)
data class LeaderboardEntry(
    val user_id: Int,
    val averageScore: Float,
    val testCount: Int
)

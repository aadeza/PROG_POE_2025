package Data_Classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "QuizScores",
    foreignKeys = [ForeignKey(
        entity = Users::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class QuizScores(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,  // ✅ Fixed casing
    val scorePercentage: Float,
    val timestamp: Long
)
data class LeaderboardEntry(
    val userId: String,
    val averageScore: Float,
    val testCount: Int
)

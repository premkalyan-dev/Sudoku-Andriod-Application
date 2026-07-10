package com.prem.skudo.database

import android.content.Context
import androidx.room.*
import com.prem.skudo.model.Difficulty
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "game_stats")
data class GameStats(
    @PrimaryKey val difficulty: String,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestTimeSeconds: Long = Long.MAX_VALUE,
    val bestTimeTimestamp: Long = 0,
    val totalTimeSeconds: Long = 0
)

@Dao
interface StatsDao {
    @Query("SELECT * FROM game_stats WHERE difficulty = :difficulty")
    suspend fun getStats(difficulty: String): GameStats?

    @Query("SELECT * FROM game_stats")
    suspend fun getAllStats(): List<GameStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: GameStats)
}

@Dao
interface GameDao {
    @Query("SELECT * FROM saved_game ORDER BY lastPlayedTimestamp DESC")
    fun observeAllSavedGames(): Flow<List<SavedGame>>

    @Query("SELECT * FROM saved_game WHERE difficulty = :difficulty")
    suspend fun getSavedGame(difficulty: String): SavedGame?

    @Query("SELECT * FROM saved_game ORDER BY lastPlayedTimestamp DESC LIMIT 1")
    suspend fun getLatestSavedGame(): SavedGame?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(game: SavedGame)

    @Query("DELETE FROM saved_game WHERE difficulty = :difficulty")
    suspend fun deleteSavedGame(difficulty: String)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    @Suppress("unused")
    suspend fun updateAchievement(achievement: Achievement)

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    @Suppress("unused")
    suspend fun getUnlockedCount(): Int
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<GameHistory>>

    @Insert
    suspend fun insertHistory(history: GameHistory)

    @Query("SELECT * FROM game_history WHERE isWin = 1 ORDER BY timeSeconds ASC LIMIT 1")
    @Suppress("unused")
    suspend fun getBestOverall(): GameHistory?
}

@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE date = :date")
    suspend fun getChallenge(date: Int): DailyChallenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: DailyChallenge)

    @Query("SELECT * FROM daily_challenges WHERE isCompleted = 1")
    fun getCompletedChallenges(): Flow<List<DailyChallenge>>
}

@Database(entities = [GameStats::class, SavedGame::class, UserProfile::class, Achievement::class, GameHistory::class, DailyChallenge::class], version = 10, exportSchema = false)
@TypeConverters(DifficultyConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statsDao(): StatsDao
    abstract fun gameDao(): GameDao
    abstract fun userDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao
    abstract fun historyDao(): HistoryDao
    abstract fun dailyChallengeDao(): DailyChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sudoku_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Drop and recreate saved_game to support per-difficulty saves and more state
                db.execSQL("DROP TABLE IF EXISTS saved_game")
                db.execSQL("""
                    CREATE TABLE saved_game (
                        difficulty TEXT NOT NULL PRIMARY KEY,
                        puzzleJson TEXT NOT NULL,
                        solutionJson TEXT NOT NULL,
                        timerSeconds INTEGER NOT NULL,
                        mistakes INTEGER NOT NULL,
                        maxMistakes INTEGER NOT NULL,
                        hintsRemaining INTEGER NOT NULL,
                        isDailyChallenge INTEGER NOT NULL,
                        lastPlayedTimestamp INTEGER NOT NULL,
                        selectedCellRow INTEGER,
                        selectedCellCol INTEGER
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_stats ADD COLUMN bestTimeTimestamp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_profile (
                        userId TEXT NOT NULL PRIMARY KEY,
                        displayName TEXT NOT NULL,
                        avatarId TEXT NOT NULL,
                        country TEXT,
                        preferredLanguage TEXT NOT NULL,
                        timezone TEXT NOT NULL,
                        joinedDate INTEGER NOT NULL,
                        lastActiveAt INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        level INTEGER NOT NULL,
                        xp INTEGER NOT NULL,
                        totalPlayTime INTEGER NOT NULL,
                        currentRank TEXT NOT NULL,
                        coins INTEGER NOT NULL,
                        gems INTEGER NOT NULL,
                        premiumStatus INTEGER NOT NULL,
                        cloudUserId TEXT,
                        email TEXT,
                        provider TEXT,
                        syncEnabled INTEGER NOT NULL,
                        lastSyncAt INTEGER
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconId TEXT NOT NULL,
                        isUnlocked INTEGER NOT NULL,
                        progress INTEGER NOT NULL,
                        target INTEGER NOT NULL,
                        unlockDate INTEGER NOT NULL,
                        xpReward INTEGER NOT NULL,
                        coinReward INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS game_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        difficulty TEXT NOT NULL,
                        timeSeconds INTEGER NOT NULL,
                        mistakes INTEGER NOT NULL,
                        hintsUsed INTEGER NOT NULL,
                        isWin INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isDaily INTEGER NOT NULL,
                        score INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_challenges (
                        date INTEGER NOT NULL PRIMARY KEY,
                        difficulty TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        completionTimeSeconds INTEGER NOT NULL,
                        completionTimestamp INTEGER NOT NULL,
                        boardData TEXT
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN winStreak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN totalXpEarned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN totalCoinsEarned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN totalGemsEarned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN highestLevel INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN highestRank TEXT NOT NULL DEFAULT 'Beginner'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN longestDailyStreak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN bestDailyChallengeTime INTEGER NOT NULL DEFAULT 9223372036854775807")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN averageCompletionTime INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN perfectGames INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN noHintWins INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN noMistakeWins INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN unlockedAvatars TEXT NOT NULL DEFAULT 'skudo_pencil'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN unlockedThemes TEXT NOT NULL DEFAULT 'Classic,Dark'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastDailyRewardTimestamp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN dailyRewardStreak INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE achievements ADD COLUMN gemReward INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN photoUrl TEXT")
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                ensureColumnExists(db, "user_profile", "hints", "INTEGER NOT NULL DEFAULT 3")
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                if (!tableExists(db, "saved_game")) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS saved_game (
                            id INTEGER PRIMARY KEY NOT NULL,
                            puzzleJson TEXT NOT NULL,
                            solutionJson TEXT NOT NULL,
                            difficulty TEXT NOT NULL,
                            timerSeconds INTEGER NOT NULL,
                            mistakes INTEGER NOT NULL,
                            maxMistakes INTEGER NOT NULL,
                            hintsRemaining INTEGER NOT NULL,
                            isDailyChallenge INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent())
                } else {
                    ensureColumnExists(db, "saved_game", "isDailyChallenge", "INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        private fun tableExists(db: androidx.sqlite.db.SupportSQLiteDatabase, tableName: String): Boolean {
            val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'")
            val exists = cursor.count > 0
            cursor.close()
            return exists
        }

        private fun ensureColumnExists(db: androidx.sqlite.db.SupportSQLiteDatabase, tableName: String, columnName: String, columnDefinition: String) {
            val cursor = db.query("PRAGMA table_info($tableName)")
            var exists = false
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == columnName) {
                    exists = true
                    break
                }
            }
            cursor.close()
            if (!exists) {
                db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
            }
        }
    }
}

class DifficultyConverter {
    @TypeConverter
    @Suppress("unused")
    fun fromDifficulty(difficulty: Difficulty): String = difficulty.name

    @TypeConverter
    @Suppress("unused")
    fun toDifficulty(name: String): Difficulty = Difficulty.valueOf(name)
}

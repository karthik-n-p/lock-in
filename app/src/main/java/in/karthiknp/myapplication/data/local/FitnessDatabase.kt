package `in`.karthiknp.myapplication.data.local

import android.content.Context
import androidx.room.*
import `in`.karthiknp.myapplication.data.local.dao.WorkoutDao
import `in`.karthiknp.myapplication.data.local.entity.Achievement
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.data.local.entity.WorkoutSession
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DailyLog::class, WorkoutSession::class, Achievement::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var INSTANCE: FitnessDatabase? = null

        private val SEED_ACHIEVEMENTS = listOf(
            Achievement("pushup_50",   "Half Century",     "Complete 50 total pushups",  WorkoutType.PUSHUP, 50),
            Achievement("pushup_100",  "Centurion",        "Complete 100 total pushups", WorkoutType.PUSHUP, 100),
            Achievement("pushup_200",  "Iron Arms",        "Complete 200 total pushups", WorkoutType.PUSHUP, 200),
            Achievement("pushup_500",  "Push Machine",     "Complete 500 total pushups", WorkoutType.PUSHUP, 500),
            Achievement("pushup_1000", "Legend",           "Complete 1000 total pushups",WorkoutType.PUSHUP, 1000),
            Achievement("plank_30",    "Steady",           "Hold plank for 30 seconds",  WorkoutType.PLANK,  30),
            Achievement("plank_60",    "Iron Core",        "Hold plank for 60 seconds",  WorkoutType.PLANK,  60),
            Achievement("plank_120",   "Stone Wall",       "Hold plank for 2 minutes",   WorkoutType.PLANK,  120),
            Achievement("plank_300",   "Immovable Object", "Hold plank for 5 minutes",   WorkoutType.PLANK,  300)
        )

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_log ADD COLUMN isStreakFix INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_db"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()

                // Seed achievements off the main thread — no runBlocking
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = instance.workoutDao()
                    SEED_ACHIEVEMENTS.forEach { achievement ->
                        if (dao.getAchievement(achievement.id) == null) {
                            dao.upsertAchievement(achievement)
                        }
                    }
                }

                INSTANCE = instance
                instance
            }
        }
    }
}

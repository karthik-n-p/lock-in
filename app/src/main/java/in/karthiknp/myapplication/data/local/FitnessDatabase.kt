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
            Achievement("pushup_10",   "First Steps",      "Complete 10 total pushups",   WorkoutType.PUSHUP, 10),
            Achievement("pushup_50",   "Half Century",     "Complete 50 total pushups",   WorkoutType.PUSHUP, 50),
            Achievement("pushup_100",  "Centurion",        "Complete 100 total pushups",  WorkoutType.PUSHUP, 100),
            Achievement("pushup_250",  "Quarter K",        "Complete 250 total pushups",  WorkoutType.PUSHUP, 250),
            Achievement("pushup_500",  "Push Machine",     "Complete 500 total pushups",  WorkoutType.PUSHUP, 500),
            Achievement("pushup_1000", "The Legend",       "Complete 1000 total pushups", WorkoutType.PUSHUP, 1000),
            Achievement("pushup_2500", "Iron Will",        "Complete 2500 total pushups", WorkoutType.PUSHUP, 2500),
            Achievement("plank_15",    "First Hold",       "Hold plank for 15 seconds",   WorkoutType.PLANK,  15),
            Achievement("plank_30",    "Steady",           "Hold plank for 30 seconds",   WorkoutType.PLANK,  30),
            Achievement("plank_60",    "Iron Core",        "Hold plank for 1 minute",     WorkoutType.PLANK,  60),
            Achievement("plank_120",   "Stone Wall",       "Hold plank for 2 minutes",    WorkoutType.PLANK,  120),
            Achievement("plank_180",   "Titanium",         "Hold plank for 3 minutes",    WorkoutType.PLANK,  180),
            Achievement("plank_300",   "Immovable",        "Hold plank for 5 minutes",    WorkoutType.PLANK,  300)
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

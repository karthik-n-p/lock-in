package `in`.karthiknp.myapplication.data.local

import androidx.room.TypeConverter
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType

class Converters {
    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String = value.name

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType = WorkoutType.valueOf(value)
}

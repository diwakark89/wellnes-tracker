package com.thewalkersoft.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thewalkersoft.tracker.data.local.converter.DateConverters
import com.thewalkersoft.tracker.data.local.dao.PeriodLogDao
import com.thewalkersoft.tracker.data.local.dao.SymptomLogDao
import com.thewalkersoft.tracker.data.local.entity.PeriodLogEntity
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity

@Database(
    entities = [PeriodLogEntity::class, SymptomLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun periodLogDao(): PeriodLogDao
    abstract fun symptomLogDao(): SymptomLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cycle_tracker.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

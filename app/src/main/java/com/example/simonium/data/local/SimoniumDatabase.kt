package com.example.simonium.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SubmissionEntity::class,
        AuditLogEntity::class,
        VerificationApplicationEntity::class,
        VerificationDocumentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SimoniumDatabase : RoomDatabase() {
    abstract fun dao(): SimoniumDao

    companion object {
        @Volatile
        private var INSTANCE: SimoniumDatabase? = null

        fun getDatabase(context: Context): SimoniumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SimoniumDatabase::class.java,
                    "simonium_platform.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

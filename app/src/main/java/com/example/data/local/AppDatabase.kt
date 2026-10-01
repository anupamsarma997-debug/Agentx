package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AppLogDao
import com.example.data.local.dao.ContentDao
import com.example.data.local.dao.ContentVersionDao
import com.example.data.local.dao.FinalVerificationDao
import com.example.data.local.dao.MemeDao
import com.example.data.local.dao.OpportunityDao
import com.example.data.local.dao.ReelDao
import com.example.data.local.dao.VerificationAuditLogDao
import com.example.data.local.entity.AppLogEntity
import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.ContentVersionEntity
import com.example.data.local.entity.FinalVerificationRecordEntity
import com.example.data.local.entity.MemeDraftEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.local.entity.ReelDraftEntity
import com.example.data.local.entity.VerificationAuditLogEntity

@Database(
    entities = [
        OpportunityEntity::class,
        ContentEntity::class,
        MemeDraftEntity::class,
        ReelDraftEntity::class,
        FinalVerificationRecordEntity::class,
        ContentVersionEntity::class,
        VerificationAuditLogEntity::class,
        AppLogEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun opportunityDao(): OpportunityDao
    abstract fun contentDao(): ContentDao
    abstract fun memeDao(): MemeDao
    abstract fun reelDao(): ReelDao
    abstract fun finalVerificationDao(): FinalVerificationDao
    abstract fun contentVersionDao(): ContentVersionDao
    abstract fun verificationAuditLogDao(): VerificationAuditLogDao
    abstract fun appLogDao(): AppLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "social_agent_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

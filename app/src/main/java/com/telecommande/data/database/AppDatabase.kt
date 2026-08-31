package com.telecommande.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.telecommande.core.protocol.TvProtocolType
import com.telecommande.data.dao.PairedTvDao
import com.telecommande.data.model.PairedTvInfo

@Database(entities = [PairedTvInfo::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pairedTvDao(): PairedTvDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE paired_tvs ADD COLUMN protocolType TEXT NOT NULL DEFAULT '${TvProtocolType.ANDROID_TV_REMOTE_V2.name}'"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "telecommande_app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

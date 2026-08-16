package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.WatchFaceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WatchFaceEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WatchFaceDatabase : RoomDatabase() {
    abstract fun watchFaceDao(): WatchFaceDao

    companion object {
        @Volatile
        private var INSTANCE: WatchFaceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WatchFaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WatchFaceDatabase::class.java,
                    "galaxy_watch_studio_db"
                )
                    .addCallback(WatchFaceDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class WatchFaceDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialPresets(database.watchFaceDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.watchFaceDao().getCount() == 0) {
                            populateInitialPresets(database.watchFaceDao())
                        }
                    }
                }
            }

            suspend fun populateInitialPresets(dao: WatchFaceDao) {
                dao.insertAll(DefaultPresets.presets)
            }
        }
    }
}

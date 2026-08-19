package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookmarkEntity
import com.example.data.model.CuratedMediaData
import com.example.data.model.HdrFormat
import com.example.data.model.MediaItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromHdrFormat(value: HdrFormat): String = value.name

    @TypeConverter
    fun toHdrFormat(value: String): HdrFormat = try {
        HdrFormat.valueOf(value)
    } catch (e: Exception) {
        HdrFormat.AUTO
    }
}

@Database(
    entities = [MediaItemEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hdr_vision_player.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).mediaDao().insertAll(CuratedMediaData.sampleStreams)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

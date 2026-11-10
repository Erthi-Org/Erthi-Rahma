package org.erthi.rahma

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "obituary_drafts")
data class ObituaryDraft(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val details: String,
    val phraseIndex: Int,
    val updatedAt: Long
)

@Dao
interface ObituaryDraftDao {
    @Query("SELECT * FROM obituary_drafts ORDER BY updatedAt DESC, id DESC")
    fun observe(): Flow<List<ObituaryDraft>>

    @Upsert
    suspend fun save(draft: ObituaryDraft): Long

    @Query("DELETE FROM obituary_drafts WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [ObituaryDraft::class], version = 1, exportSchema = false)
abstract class ObituaryDraftDatabase : RoomDatabase() {
    abstract fun drafts(): ObituaryDraftDao

    companion object {
        private var instance: ObituaryDraftDatabase? = null

        @Synchronized
        fun open(context: Context): ObituaryDraftDatabase =
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ObituaryDraftDatabase::class.java,
                "obituary-drafts.db"
            ).build().also { instance = it }
    }
}

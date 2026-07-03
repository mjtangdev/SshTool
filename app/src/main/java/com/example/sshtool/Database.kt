package com.example.sshtool

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "ssh_hosts")
data class SshHostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val host: String,
    val user: String,
    val pass: String,
    val scriptsJson: String = "" // Simple storage for scripts list
)

@Dao
interface SshHostDao {
    @Query("SELECT * FROM ssh_hosts")
    fun getAllHosts(): Flow<List<SshHostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHost(host: SshHostEntity)

    @Delete
    suspend fun deleteHost(host: SshHostEntity)
}

@Database(entities = [SshHostEntity::class], version = 2, exportSchema = false)
abstract class SshDatabase : RoomDatabase() {
    abstract fun sshHostDao(): SshHostDao

    companion object {
        @Volatile
        private var INSTANCE: SshDatabase? = null

        fun getDatabase(context: Context): SshDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SshDatabase::class.java,
                    "ssh_database"
                )
                .fallbackToDestructiveMigration() // Simple for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

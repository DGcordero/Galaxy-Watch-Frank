package com.example.data

import androidx.room.*
import com.example.model.WatchFaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchFaceDao {
    @Query("SELECT * FROM watch_faces ORDER BY isCurrentActive DESC, createdAtTimestamp DESC")
    fun getAllWatchFaces(): Flow<List<WatchFaceEntity>>

    @Query("SELECT * FROM watch_faces WHERE id = :id LIMIT 1")
    fun getWatchFaceById(id: String): Flow<WatchFaceEntity?>

    @Query("SELECT * FROM watch_faces WHERE isFavorite = 1 ORDER BY createdAtTimestamp DESC")
    fun getFavoriteWatchFaces(): Flow<List<WatchFaceEntity>>

    @Query("SELECT * FROM watch_faces WHERE isCustomUserCreated = 1 ORDER BY createdAtTimestamp DESC")
    fun getUserCreatedWatchFaces(): Flow<List<WatchFaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchFace(watchFace: WatchFaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(watchFaces: List<WatchFaceEntity>)

    @Update
    suspend fun updateWatchFace(watchFace: WatchFaceEntity)

    @Delete
    suspend fun deleteWatchFace(watchFace: WatchFaceEntity)

    @Query("UPDATE watch_faces SET isCurrentActive = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setActiveWatchFace(id: String)

    @Query("UPDATE watch_faces SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM watch_faces")
    suspend fun getCount(): Int
}

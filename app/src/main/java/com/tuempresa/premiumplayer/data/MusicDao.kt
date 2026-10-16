package com.tuempresa.premiumplayer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tuempresa.premiumplayer.data.entities.PlayEvent
import com.tuempresa.premiumplayer.data.entities.Playlist
import com.tuempresa.premiumplayer.data.entities.PlaylistSongCrossRef
import com.tuempresa.premiumplayer.data.entities.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // ---------- CANCIONES ----------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<Song>>

    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT 50")
    fun getMostPlayed(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT 50")
    fun getRecentlyPlayed(): Flow<List<Song>>

    @Query(
        "SELECT * FROM songs WHERE " +
        "title LIKE '%' || :query || '%' OR " +
        "artist LIKE '%' || :query || '%' OR " +
        "album LIKE '%' || :query || '%' " +
        "ORDER BY title ASC"
    )
    fun searchSongs(query: String): Flow<List<Song>>

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun recordPlay(songId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSong(songId: Long)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    // ---------- PLAYLISTS ----------

    @Insert
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(crossRef: PlaylistSongCrossRef)

    @Query(
        "SELECT s.* FROM songs s " +
        "INNER JOIN playlist_song_cross_ref p ON s.id = p.songId " +
        "WHERE p.playlistId = :playlistId " +
        "ORDER BY p.position ASC"
    )
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    // ---------- EVENTOS DE REPRODUCCIÓN ----------

    @Insert
    suspend fun insertPlayEvent(event: PlayEvent)

    @Query("SELECT * FROM play_events ORDER BY timestamp DESC LIMIT 100")
    fun getRecentPlayEvents(): Flow<List<PlayEvent>>
}

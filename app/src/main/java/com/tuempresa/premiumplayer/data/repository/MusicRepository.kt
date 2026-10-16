package com.tuempresa.premiumplayer.data.repository

import android.content.Context
import android.net.Uri
import com.tuempresa.premiumplayer.data.MusicDao
import com.tuempresa.premiumplayer.data.entities.PlayEvent
import com.tuempresa.premiumplayer.data.entities.Playlist
import com.tuempresa.premiumplayer.data.entities.PlaylistSongCrossRef
import com.tuempresa.premiumplayer.data.entities.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio central de música.
 *
 * Es la fachada que usa la app para acceder a la biblioteca:
 * combina el DAO (base de datos), el escáner (MediaStore) y
 * el extractor de metadatos (archivos individuales).
 */
class MusicRepository(
    private val context: Context,
    private val dao: MusicDao
) {

    private val scanner = LibraryScanner(context)
    private val extractor = MetadataExtractor(context)

    // ---------- CONSULTAS ----------

    fun getAllSongs(): Flow<List<Song>> = dao.getAllSongs()

    fun getFavorites(): Flow<List<Song>> = dao.getFavorites()

    fun getMostPlayed(): Flow<List<Song>> = dao.getMostPlayed()

    fun getRecentlyPlayed(): Flow<List<Song>> = dao.getRecentlyPlayed()

    fun searchSongs(query: String): Flow<List<Song>> = dao.searchSongs(query)

    suspend fun getSongById(id: Long): Song? = dao.getSongById(id)

    suspend fun getSongCount(): Int = dao.getSongCount()

    // ---------- SINCRONIZACIÓN ----------

    /**
     * Escanea MediaStore y guarda/actualiza las canciones en la
     * base de datos. Devuelve cuántas canciones se encontraron.
     */
    suspend fun syncLibrary(): Int {
        val songs = scanner.scanMediaStore()
        if (songs.isNotEmpty()) {
            dao.insertSongs(songs)
        }
        return songs.size
    }

    /**
     * Importa archivos seleccionados manualmente por el usuario.
     * Devuelve cuántos se importaron con éxito.
     */
    suspend fun importFiles(uris: List<Uri>): Int {
        var imported = 0
        val songs = mutableListOf<Song>()

        for (uri in uris) {
            val song = extractor.extractFromUri(uri)
            if (song != null) {
                songs.add(song)
                imported++
            }
        }

        if (songs.isNotEmpty()) {
            dao.insertSongs(songs)
        }
        return imported
    }

    // ---------- FAVORITOS Y REPRODUCCIÓN ----------

    suspend fun toggleFavorite(song: Song) {
        dao.updateFavorite(song.id, !song.isFavorite)
    }

    suspend fun setFavorite(songId: Long, isFavorite: Boolean) {
        dao.updateFavorite(songId, isFavorite)
    }

    suspend fun recordPlay(songId: Long) {
        dao.recordPlay(songId)
        dao.insertPlayEvent(PlayEvent(songId = songId))
    }

    suspend fun deleteSong(songId: Long) {
        dao.deleteSong(songId)
    }

    // ---------- PLAYLISTS ----------

    fun getAllPlaylists(): Flow<List<Playlist>> = dao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return dao.insertPlaylist(Playlist(name = name))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int) {
        dao.insertPlaylistSong(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                position = position
            )
        )
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        dao.getSongsInPlaylist(playlistId)

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylist(playlistId)
    }
}

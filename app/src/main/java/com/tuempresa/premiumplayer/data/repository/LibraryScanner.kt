package com.tuempresa.premiumplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.tuempresa.premiumplayer.data.entities.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Escanea la biblioteca musical del dispositivo usando MediaStore.
 *
 * Devuelve una lista de objetos Song con los metadatos
 * que Android haya indexado (título, artista, álbum, duración, etc.).
 */
class LibraryScanner(private val context: Context) {

    suspend fun scanMediaStore(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val cursor = try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )
        } catch (e: Exception) {
            null
        }

        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (c.moveToNext()) {
                try {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Sin título"
                    val artist = c.getString(artistCol)
                        ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                        ?: "Artista desconocido"
                    val album = c.getString(albumCol)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Álbum desconocido"
                    val albumId = c.getLong(albumIdCol)
                    val duration = c.getLong(durationCol)
                    val track = c.getInt(trackCol)
                    val path = c.getString(dataCol) ?: ""
                    val dateAdded = c.getLong(dateCol)

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            duration = duration,
                            trackNumber = track,
                            uri = uri,
                            albumArtUri = albumArtUri,
                            folderPath = path.substringBeforeLast("/", ""),
                            dateAdded = dateAdded
                        )
                    )
                } catch (e: Exception) {
                    // Ignoramos esta canción si tiene datos corruptos
                    continue
                }
            }
        }

        songs.distinctBy { it.uri }
    }
}

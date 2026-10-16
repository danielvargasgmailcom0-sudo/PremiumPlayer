package com.tuempresa.premiumplayer.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.tuempresa.premiumplayer.data.entities.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Extrae metadatos de un archivo de audio concreto usando
 * MediaMetadataRetriever. Se usa cuando el usuario selecciona
 * archivos manualmente desde el selector de documentos.
 *
 * Devuelve null si el archivo está corrupto o es inaccesible.
 */
class MetadataExtractor(private val context: Context) {

    suspend fun extractFromUri(uri: Uri): Song? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            val title = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: uri.lastPathSegment
                    ?.substringBeforeLast(".")
                    ?.takeIf { it.isNotBlank() }
                ?: "Sin título"

            val artist = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: "Artista desconocido"

            val album = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it.isNotBlank() }
                ?: "Álbum desconocido"

            val duration = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            val track = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.toIntOrNull() ?: 0

            val embeddedPicture = retriever.embeddedPicture
            val albumArtUri = embeddedPicture?.let {
                saveEmbeddedArt(it, uri.hashCode().toString())
            }

            Song(
                id = uri.hashCode().toLong(),
                title = title,
                artist = artist,
                album = album,
                albumId = 0L,
                duration = duration,
                trackNumber = track,
                uri = uri.toString(),
                albumArtUri = albumArtUri,
                folderPath = uri.path?.substringBeforeLast("/", "") ?: "",
                dateAdded = System.currentTimeMillis() / 1000
            )
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun saveEmbeddedArt(data: ByteArray, key: String): String? {
        return try {
            val file = File(context.cacheDir, "embedded_art_$key.jpg")
            if (!file.exists()) {
                file.writeBytes(data)
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            null
        }
    }
}

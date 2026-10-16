package com.tuempresa.premiumplayer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una canción en la biblioteca musical.
 *
 * Cada fila de la tabla "songs" en la base de datos Room
 * es un objeto Song.
 */
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val trackNumber: Int,
    val uri: String,
    val albumArtUri: String?,
    val folderPath: String,
    val dateAdded: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
)

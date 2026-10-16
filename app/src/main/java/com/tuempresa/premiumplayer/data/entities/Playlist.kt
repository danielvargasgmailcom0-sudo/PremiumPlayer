package com.tuempresa.premiumplayer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una lista de reproducción creada por el usuario.
 *
 * Cada fila de la tabla "playlists" en la base de datos Room
 * es un objeto Playlist.
 */
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

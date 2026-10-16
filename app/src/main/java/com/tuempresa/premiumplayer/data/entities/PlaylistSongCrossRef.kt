package com.tuempresa.premiumplayer.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Tabla intermedia que relaciona playlists con canciones.
 *
 * Cada fila significa: "La canción X está en la playlist Y,
 * en la posición Z".
 *
 * Si se borra una playlist o una canción, Room borra
 * automáticamente las filas asociadas (onDelete = CASCADE).
 */
@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val position: Int
)

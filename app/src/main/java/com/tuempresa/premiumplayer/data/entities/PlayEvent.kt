package com.tuempresa.premiumplayer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un evento de reproducción.
 *
 * Cada vez que una canción empieza a sonar, se inserta
 * una nueva fila aquí. Sirve para construir el historial
 * de reproducciones y las estadísticas.
 */
@Entity(tableName = "play_events")
data class PlayEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val timestamp: Long = System.currentTimeMillis()
)

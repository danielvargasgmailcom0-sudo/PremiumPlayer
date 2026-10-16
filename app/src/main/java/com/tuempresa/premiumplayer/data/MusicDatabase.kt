package com.tuempresa.premiumplayer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tuempresa.premiumplayer.data.entities.PlayEvent
import com.tuempresa.premiumplayer.data.entities.Playlist
import com.tuempresa.premiumplayer.data.entities.PlaylistSongCrossRef
import com.tuempresa.premiumplayer.data.entities.Song

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSongCrossRef::class,
        PlayEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}

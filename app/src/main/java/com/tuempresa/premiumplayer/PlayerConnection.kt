package com.tuempresa.premiumplayer

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Puente entre la interfaz (Compose) y el servicio de reproducción.
 *
 * Se conecta a MediaSessionService mediante un MediaController
 * y expone el estado del reproductor como StateFlow para que
 * la UI lo observe reactivamente.
 */
class PlayerConnection(
    private val context: Context,
    private val scope: CoroutineScope
) : Player.Listener {

    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _error = MutableStateFlow<PlaybackException?>(null)
    val error: StateFlow<PlaybackException?> = _error.asStateFlow()

    private var progressJob: Job? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    /**
     * Conecta al servicio de reproducción.
     * Debe llamarse una vez al inicio de la app.
     */
    fun connect() {
        if (_controller.value != null) return

        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener(
            {
                val c = controllerFuture?.get()
                if (c != null) {
                    _controller.value = c
                    c.addListener(this)
                    syncInitialState(c)
                    startProgressTracking(c)
                }
            },
            { runnable -> scope.launch(Dispatchers.Main) { runnable.run() } }
        )
    }

    private fun syncInitialState(c: MediaController) {
        _isPlaying.value = c.isPlaying
        _currentMediaItem.value = c.currentMediaItem
        _duration.value = c.duration.coerceAtLeast(0L)
        _repeatMode.value = c.repeatMode
        _shuffleEnabled.value = c.shuffleModeEnabled
        _currentPosition.value = c.currentPosition.coerceAtLeast(0L)
    }

    private fun startProgressTracking(c: MediaController) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                try {
                    if (c.isPlaying) {
                        _currentPosition.value = c.currentPosition
                        val d = c.duration
                        if (d > 0) _duration.value = d
                    }
                } catch (e: Exception) {
                    // Ignorar excepciones transitorias
                }
                delay(250L)
            }
        }
    }

    // ------- Callbacks del Player -------

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val c = _controller.value ?: return
        if (playbackState == Player.STATE_READY) {
            val d = c.duration
            if (d > 0) _duration.value = d
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        _currentMediaItem.value = mediaItem
        _currentPosition.value = 0L
        _duration.value = _controller.value?.duration?.coerceAtLeast(0L) ?: 0L
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        _repeatMode.value = repeatMode
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        _shuffleEnabled.value = shuffleModeEnabled
    }

    override fun onPlayerError(error: PlaybackException) {
        _error.value = error
    }

    // ------- Controles expuestos a la UI -------

    fun play() { _controller.value?.play() }

    fun pause() { _controller.value?.pause() }

    fun playPause() {
        val c = _controller.value ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { _controller.value?.seekToNextMediaItem() }

    fun previous() { _controller.value?.seekToPreviousMediaItem() }

    fun seekTo(positionMs: Long) {
        _controller.value?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun setRepeatMode(mode: Int) {
        _controller.value?.repeatMode = mode
    }

    fun toggleShuffle() {
        val c = _controller.value ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Libera los recursos. Debe llamarse cuando la app se cierra.
     */
    fun release() {
        progressJob?.cancel()
        _controller.value?.removeListener(this)
        controllerFuture?.let {
            try { MediaController.releaseFuture(it) } catch (_: Exception) {}
        }
        _controller.value = null
        controllerFuture = null
    }
}

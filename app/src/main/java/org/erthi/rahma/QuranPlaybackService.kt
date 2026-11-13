package org.erthi.rahma

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

internal fun Context.quranProgress() = getSharedPreferences("quran_progress", Context.MODE_PRIVATE)
internal const val LAST_RECITATION = "recitation"
internal const val LAST_POSITION = "position_ms"

/** Media3 foreground-capable media session for Quran playback. */
class QuranPlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    private val handler = Handler(Looper.getMainLooper())
    private val checkpoint = object : Runnable {
        override fun run() {
            val player = session?.player ?: return
            if (player.isPlaying) saveProgress(player)
            handler.postDelayed(this, 5_000)
        }
    }

    private fun saveProgress(player: Player) {
        val mediaId = player.currentMediaItem?.mediaId ?: return
        if (QuranRecording.fromId(mediaId) == null) return
        val position = if (player.playbackState == Player.STATE_ENDED) 0L else player.currentPosition
        quranProgress().edit().putString(LAST_RECITATION, mediaId)
            .putLong(LAST_POSITION, position.coerceAtLeast(0)).apply()
    }

    override fun onCreate() {
        super.onCreate()
        val attributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(attributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                    events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                    events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
                ) saveProgress(player)
            }
        })
        session = MediaSession.Builder(this, player).build()
        handler.postDelayed(checkpoint, 5_000)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        handler.removeCallbacks(checkpoint)
        session?.run {
            saveProgress(player)
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}

package com.elderlylauncher.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log

/** Plays a short sample on one sound channel, so the volume can be checked by ear. */
object SoundTester {
    enum class Channel { MEDIA, NOTIFICATION, ALARM }

    private const val TAG = "SoundTester"
    private const val PLAY_MS = 3000L

    private val handler = Handler(Looper.getMainLooper())
    private var current: Ringtone? = null
    private val stopRunnable = Runnable { stop() }

    fun play(context: Context, channel: Channel) {
        stop()
        val (type, usage, stream) = when (channel) {
            Channel.MEDIA -> Triple(RingtoneManager.TYPE_RINGTONE, AudioAttributes.USAGE_MEDIA, AudioManager.STREAM_MUSIC)
            Channel.NOTIFICATION -> Triple(RingtoneManager.TYPE_NOTIFICATION, AudioAttributes.USAGE_NOTIFICATION, AudioManager.STREAM_NOTIFICATION)
            Channel.ALARM -> Triple(RingtoneManager.TYPE_ALARM, AudioAttributes.USAGE_ALARM, AudioManager.STREAM_ALARM)
        }
        val ringtone = try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(context, type)
                ?: RingtoneManager.getDefaultUri(type)
            RingtoneManager.getRingtone(context.applicationContext, uri)
        } catch (e: Exception) {
            Log.e(TAG, "No sample sound", e)
            null
        }
        if (ringtone == null) {
            beep(stream)
            return
        }
        try {
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(usage)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
            current = ringtone
            handler.postDelayed(stopRunnable, PLAY_MS)
        } catch (e: Exception) {
            Log.e(TAG, "Could not play sample", e)
            beep(stream)
        }
    }

    fun stop() {
        handler.removeCallbacks(stopRunnable)
        try {
            current?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Could not stop sample", e)
        }
        current = null
    }

    private fun beep(stream: Int) {
        try {
            val tone = ToneGenerator(stream, ToneGenerator.MAX_VOLUME)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
            handler.postDelayed({ tone.release() }, 800)
        } catch (e: Exception) {
            Log.e(TAG, "Could not beep", e)
        }
    }
}

package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.SoundCue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SoundEffects {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  fun play(cue: SoundCue, enabled: Boolean) {
    if (!enabled) return
    scope.launch {
      when (cue) {
        SoundCue.MOVE -> playToneSequence(listOf(220.0 to 55))
        SoundCue.PICKUP -> playToneSequence(listOf(523.25 to 70, 659.25 to 90))
        SoundCue.SOLVE -> playToneSequence(listOf(440.0 to 80, 554.37 to 80, 659.25 to 140))
        SoundCue.RESTORE -> playToneSequence(listOf(392.0 to 80, 523.25 to 120))
        SoundCue.WARNING -> playToneSequence(listOf(196.0 to 110, 164.81 to 130))
        SoundCue.VICTORY ->
          playToneSequence(listOf(523.25 to 100, 659.25 to 100, 783.99 to 120, 1046.5 to 220))
        SoundCue.DEFEAT -> playToneSequence(listOf(246.94 to 140, 220.0 to 160, 174.61 to 260))
      }
    }
  }

  private fun playToneSequence(notes: List<Pair<Double, Int>>) {
    try {
      val sampleRate = 22050
      val totalSamples = notes.sumOf { (_, ms) -> (sampleRate * ms) / 1000 }
      val pcm = ShortArray(totalSamples)
      var offset = 0

      for ((freq, ms) in notes) {
        val count = (sampleRate * ms) / 1000
        for (i in 0 until count) {
          val t = i.toDouble() / sampleRate
          val env = exp(-t * 10.0) * (1.0 - (i.toDouble() / count) * 0.2)
          val sample = sin(2.0 * PI * freq * t) * env * 0.25
          pcm[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        offset += count
      }

      val track =
        AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(pcm.size * 2)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

      track.write(pcm, 0, pcm.size)
      track.play()
      val totalDurationMs = notes.sumOf { it.second }.toLong() + 60L
      Thread.sleep(totalDurationMs)
      track.stop()
      track.release()
    } catch (_: Throwable) {
      // Audio playback is strictly best-effort and never interrupts gameplay.
    }
  }
}

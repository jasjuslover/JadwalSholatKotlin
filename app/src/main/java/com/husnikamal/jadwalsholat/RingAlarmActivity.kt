package com.husnikamal.jadwalsholat

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.husnikamal.jadwalsholat.databinding.ActivityRingAlarmBinding

class RingAlarmActivity : AppCompatActivity() {

    private lateinit var player: MediaPlayer

    private lateinit var binding: ActivityRingAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ring_alarm)

        player = MediaPlayer.create(this, R.raw.azan)
        player?.start()
        player?.isLooping = false

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ring_alarm)

        binding.imageViewStop.setOnClickListener {
            if (player.isPlaying) {
                player?.pause()
                binding.imageViewStop.setImageResource(R.drawable.ic_play)
            } else {
                player?.start()
                binding.imageViewStop.setImageResource(R.drawable.ic_stop)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (player != null && player!!.isPlaying) {
            player?.stop()
            player?.reset()
            player?.release()
        }
    }
}

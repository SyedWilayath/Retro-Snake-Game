package com.nokiasnake.game.sound

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.nokiasnake.game.R

class SoundManager(private val context: Context) {
    private var movePlayer: ExoPlayer? = null
    private var eatPlayer: ExoPlayer? = null
    private var gameOverPlayer: ExoPlayer? = null

    private val moveMediaItem: MediaItem
    private val eatMediaItem: MediaItem
    private val gameOverMediaItem: MediaItem

    init {
        val packageName = context.packageName
        moveMediaItem = MediaItem.fromUri(Uri.parse("android.resource://$packageName/${R.raw.move_sound}"))
        eatMediaItem = MediaItem.fromUri(Uri.parse("android.resource://$packageName/${R.raw.eat_sound}"))
        gameOverMediaItem = MediaItem.fromUri(Uri.parse("android.resource://$packageName/${R.raw.game_over_sound}"))
        initializePlayers()
    }

    private fun initializePlayers() {
        movePlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(moveMediaItem)
            prepare()
            setRepeatMode(Player.REPEAT_MODE_OFF)
        }

        eatPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(eatMediaItem)
            prepare()
            setRepeatMode(Player.REPEAT_MODE_OFF)
        }

        gameOverPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(gameOverMediaItem)
            prepare()
            setRepeatMode(Player.REPEAT_MODE_OFF)
        }
    }

    fun playMoveSound() {
        movePlayer?.seekTo(0)
        movePlayer?.playWhenReady = true
    }

    fun playEatSound() {
        eatPlayer?.seekTo(0)
        eatPlayer?.playWhenReady = true
    }

    fun playGameOverSound() {
        gameOverPlayer?.seekTo(0)
        gameOverPlayer?.playWhenReady = true
    }

    fun release() {
        movePlayer?.release()
        movePlayer = null
        eatPlayer?.release()
        eatPlayer = null
        gameOverPlayer?.release()
        gameOverPlayer = null
    }
}
/*
 * Copyright 2018 Koma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.koma.video.play

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.koma.video.R
import com.koma.video.data.enities.VideoEntry


class MediaController @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs), View.OnClickListener {
    private lateinit var pause: ImageView
    private lateinit var rotation: ImageView
    private lateinit var currentTime: TextView
    private lateinit var duration: TextView
    private lateinit var progress: SeekBar

    private var isDragging = false
    private var isShowing = false

    init {
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.media_controller_background))
        radius = 8f
        cardElevation = 0f
        useCompatPadding = true
    }

    private lateinit var player: MediaPlayerControl

    fun setMediaPlayer(playerControl: MediaPlayerControl) {
        this.player = playerControl

        updatePausePlay()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        View.inflate(context, R.layout.media_controller, this)

        initView()
    }

    private fun initView() {
        pause = findViewById(R.id.iv_pause)
        pause.setOnClickListener(this)

        currentTime = findViewById(R.id.tv_current)

        duration = findViewById(R.id.tv_duration)

        progress = findViewById(R.id.seek_bar)
        progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromuser: Boolean) {
                if (!fromuser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return
                }

                val duration = player.duration
                val newposition = duration * progress / 1000L
                player.seekTo(newposition.toInt())
                currentTime.text = VideoEntry.stringForTime(newposition.toInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                show(3600000)
                isDragging = true
                // By removing these pending progress messages we make sure
                // that a) we won't update the progress while the user adjusts
                // the seekbar and b) once the user is done dragging the thumb
                // we will post one of these messages to the queue again and
                // this ensures that there will be exactly one message queued up.
                removeCallbacks(showProgress);
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isDragging = false

                setProgress()

                updatePausePlay()

                show(DEAULT_TIME_OUT)

                // Ensure that progress is properly updated in the future,
                // the call to show() does not guarantee this because it is a
                // no-op if we are already isShowing.
                post(showProgress)
            }
        })

        rotation = findViewById(R.id.iv_fullscreen)
        rotation.setOnClickListener(this)
    }

    override fun setEnabled(enabled: Boolean) {
        pause.isEnabled = enabled
        progress.isEnabled = enabled
        super.setEnabled(enabled)
    }

    fun show() {
        show(DEAULT_TIME_OUT)
    }

    fun show(timeout: Int) {
        if (!isShowing) {
            setProgress()

            isShowing = true
        }
        updatePausePlay()

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar isShowing the user hits play.
        post(showProgress);

        if (timeout != 0) {
            removeCallbacks(fadeOut);
            postDelayed(fadeOut, timeout.toLong())
        }
    }

    private fun setProgress(): Int {
        if (isDragging) {
            return 0
        }
        val position = player.currentPosition
        val duration = player.duration
        if (duration > 0) {
            // use long to avoid overflow
            val pos = 1000L * position / duration
            progress.progress = pos.toInt()
        }
        val percent = player.bufferPercentage
        progress.secondaryProgress = percent * 10
        this.duration.text = VideoEntry.stringForTime(duration)
        currentTime.text = VideoEntry.stringForTime(position)

        return position
    }

    fun isShowing(): Boolean {
        return isShowing
    }

    /**
     * Remove the controller from the screen.
     */
    fun hide() {
        if (isShowing) {
            removeCallbacks(showProgress)

            isShowing = false
        }
    }

    private val fadeOut = Runnable { hide() }

    private val showProgress = object : Runnable {
        override fun run() {
            val pos = setProgress()
            if (!isDragging && isShowing && player.isPlaying) {
                postDelayed(this, (1000 - pos % 1000).toLong())
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_pause -> {
                doPauseResume()

                show(DEAULT_TIME_OUT)
            }
            R.id.iv_fullscreen -> {

            }
        }
    }

    private fun updatePausePlay() {
        if (player.isPlaying) {
            pause.setImageResource(R.drawable.ic_pause)
        } else {
            pause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun doPauseResume() {
        with(player) {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
        }
        updatePausePlay()
    }

    companion object {
        private const val TAG = "MediaController"

        private const val DEAULT_TIME_OUT = 3000
    }
}
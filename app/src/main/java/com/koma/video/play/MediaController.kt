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
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import com.koma.video.R
import com.koma.video.data.enities.VideoEntry
import kotlinx.android.synthetic.main.media_controller.view.*

class MediaController @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), View.OnClickListener {
    private var isDragging = false
    private var isShowing = false

    private lateinit var player: MediaPlayerControl

    init {
        fitsSystemWindows = true
        visibility = View.GONE
    }

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
        iv_pause.setOnClickListener(this)
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromuser: Boolean) {
                if (!fromuser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return
                }

                val duration = player.duration
                val newPosition = duration * progress / 1000L
                player.seekTo(newPosition.toInt())
                tv_current.text = VideoEntry.stringForTime(newPosition.toInt())
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

                show(DEFAULT_TIME_OUT)

                // Ensure that progress is properly updated in the future,
                // the call to show() does not guarantee this because it is a
                // no-op if we are already isShowing.
                post(showProgress)
            }
        })
    }

    override fun setEnabled(enabled: Boolean) {
        iv_pause.isEnabled = enabled
        seek_bar.isEnabled = enabled
        super.setEnabled(enabled)
    }

    fun show() {
        show(DEFAULT_TIME_OUT)
    }

    fun show(timeout: Int) {
        if (!isShowing) {
            setProgress()

            (context as PlayActivity).showSystemUi(true)

            visibility = View.VISIBLE

            isShowing = true
        }
        updatePausePlay()

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar isShowing the user hits play.
        post(showProgress)

        if (timeout != 0) {
            removeCallbacks(fadeOut)
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
            seek_bar.progress = pos.toInt()
        }
        val percent = player.bufferPercentage
        seek_bar.secondaryProgress = percent * 10
        tv_duration.text = VideoEntry.stringForTime(duration)
        tv_current.text = VideoEntry.stringForTime(position)

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
            (context as PlayActivity).showSystemUi(false)

            visibility = View.GONE

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

                show(DEFAULT_TIME_OUT)
            }
        }
    }

    private fun updatePausePlay() {
        if (player.isPlaying) {
            iv_pause.setImageResource(R.drawable.ic_pause)
        } else {
            iv_pause.setImageResource(R.drawable.ic_play)
        }
    }

    fun doPauseResume() {
        with(player) {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
        }
        updatePausePlay()
    }

    fun setLoadingIndicator(active: Boolean) {
        if (active) {
            progress_bar.show()
        } else {
            progress_bar.hide()
        }
    }

    companion object {
        private const val TAG = "MediaController"

        private const val DEFAULT_TIME_OUT = 3000
    }
}
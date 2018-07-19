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
import android.graphics.Point
import android.media.AudioManager
import android.provider.Settings
import android.support.annotation.IntDef
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.koma.video.util.LogUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GestureProcessor constructor(private val context: Context) :
    GestureDetector.SimpleOnGestureListener() {
    private val window = (context as PlayActivity).window

    private var audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var screenWidth: Int = 0
        get() = getWindowWidth()
    private var screenHeight: Int = 0
        get() = getWindowHeight()

    private var currentBrightness: Float = window.attributes.screenBrightness

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    private var currentVolume: Int = 0

    @Mode
    private var mode = MODE_UNKNOWN

    private var listener: GestureListener? = null

    fun setOnGestureListener(listener: GestureListener) {
        this.listener = listener
    }

    private fun getWindowWidth(): Int {
        val point = Point()
        window.windowManager.defaultDisplay.getSize(point)
        return point.x
    }

    private fun getWindowHeight(): Int {
        val point = Point()
        window.windowManager.defaultDisplay.getSize(point)
        return point.y
    }

    override fun onDown(e: MotionEvent?): Boolean {
        mode = MODE_UNKNOWN

        currentBrightness = window.attributes.screenBrightness
        if (currentBrightness < 0) {
            currentBrightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255f
        }

        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        LogUtils.d(
            TAG,
            "onDown mode $mode,currentBrightness$currentBrightness,currentVolume$currentVolume"
        )

        return super.onDown(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        LogUtils.d(TAG, "111 onScroll mode : $mode")
        if (e1 == null || e2 == null) {
            mode = MODE_UNKNOWN

            return super.onScroll(e1, e2, distanceX, distanceY)
        }
        val dx = e2.x - e1.x
        val dy = e1.y - e2.y
        LogUtils.d(TAG, "111 onScroll dx : $dx,dy :$dy,touchSlop : $touchSlop")
        when (mode) {
            MODE_UNKNOWN -> {
                if (dx * dx + dy * dy > touchSlop * touchSlop) {
                    if (abs(dx) > abs(dy)) {
                        mode = MODE_PROGRESS
                    } else if (abs(dx) < abs(dy)) {
                        mode = if (e1.x < screenWidth / 2) {
                            MODE_BRIGHTNESS
                        } else {
                            MODE_VOLUME
                        }
                    }
                } else {
                    mode = MODE_UNKNOWN
                }
            }
            MODE_PROGRESS -> {

            }
            MODE_BRIGHTNESS -> {
                onBrightnessChanged(dy)
            }
            MODE_VOLUME -> {
                onVolumeChange(dy)
            }
        }

        LogUtils.d(TAG, "222 onScroll mode : $mode")

        return true
    }

    private fun onVolumeChange(dy: Float) {
        var volume = currentVolume + (dy / screenHeight * maxVolume).toInt()
        volume = min(volume, maxVolume)
        volume = max(volume, 0)
        LogUtils.d(
            TAG,
            "currentVolume $currentVolume,volume $volume,maxVolume $maxVolume,percent ${volume / maxVolume}"
        )
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            AudioManager.MODE_NORMAL
        )
        listener?.onVolumeChanged(volume * 100 / maxVolume)
    }

    private fun onBrightnessChanged(dy: Float) {
        var brightness = currentBrightness + dy / screenHeight
        brightness = min(brightness, 1.0f)
        brightness = max(brightness, 0.0f)
        LogUtils.d(TAG, "currentBrightness $currentBrightness,brightness $brightness")

        val params = window.attributes
        params.screenBrightness = brightness
        window.attributes = params

        listener?.onBrightnessChanged((brightness * 100).toInt())
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return listener?.onSingleTapConfirmed() ?: super.onSingleTapConfirmed(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        LogUtils.d(TAG, "onDoubleTap")

        return listener?.onDoubleTap() ?: super.onDoubleTap(e)
    }

    companion object {
        private const val TAG = "GestureProcessor"

        private const val MODE_UNKNOWN = 0
        private const val MODE_BRIGHTNESS = 1
        private const val MODE_VOLUME = 2
        private const val MODE_PROGRESS = 3

        @IntDef(MODE_UNKNOWN, MODE_BRIGHTNESS, MODE_VOLUME, MODE_PROGRESS)
        annotation class Mode
    }
}
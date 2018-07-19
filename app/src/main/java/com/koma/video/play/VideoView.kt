/*
 * Copyright 2018 koma_mj
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
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.koma.video.util.LogUtils
import java.io.IOException


class VideoView constructor(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs), MediaPlayerControl {
    private var videoWidth = 0
    private var videoHeight = 0
    private var surfaceWidth = 0
    private var surfaceHeight = 0

    // currentState is a VideoView object's current state.
    // targetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private var currentState = STATE_IDLE
    private var targetState = STATE_IDLE

    // All the stuff we need for playing and showing a video
    private var surfaceHolder: SurfaceHolder? = null

    private var mediaPlayer: MediaPlayer? = null

    private var audioSession: Int = 0

    // settable by the client
    private var uri: Uri? = null

    private var mediaController: MediaController? = null
    private var onCompletionListener: MediaPlayer.OnCompletionListener? = null
    private var onPreparedListener: MediaPlayer.OnPreparedListener? = null
    private var onErrorListener: MediaPlayer.OnErrorListener? = null
    private var onInfoListener: MediaPlayer.OnInfoListener? = null
    private var seekWhenPrepared: Int = 0  // recording the seek position while preparing
    private val audioManager: AudioManager
    private var audioFocusType = AudioManager.AUDIOFOCUS_GAIN // legacy focus gain
    private val audioAttributes: AudioAttributes

    private val callback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(
            holder: SurfaceHolder, format: Int,
            w: Int, h: Int
        ) {
            LogUtils.i(TAG, "surfaceChanged")
            surfaceWidth = w
            surfaceHeight = h
            val isValidState = targetState == STATE_PLAYING
            val hasValidSize = videoWidth == w && videoHeight == h
            if (isValidState && hasValidSize) {
                if (seekWhenPrepared != 0) {
                    seekTo(seekWhenPrepared)
                }
                start()
            }
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            LogUtils.i(TAG, "surfaceCreated")

            mediaController?.setLoadingIndicator(true)

            surfaceHolder = holder

            openVideo()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            LogUtils.i(TAG, "surfaceDestroyed")
            // after we return from this we can't use the surface any more
            surfaceHolder = null

            mediaController?.hide()

            release(true)
        }
    }

    init {
        videoWidth = 0
        videoHeight = 0

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build()

        holder.addCallback(callback)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()

        currentState = STATE_IDLE
        targetState = STATE_IDLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = getDefaultSize(videoWidth, widthMeasureSpec)
        var height = getDefaultSize(videoHeight, heightMeasureSpec)
        if (videoWidth > 0 && videoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * videoHeight / videoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * videoWidth / videoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth
                height = videoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            }
        }
        setMeasuredDimension(width, height)
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    fun setVideoPath(path: String) {
        setVideoURI(Uri.parse(path))
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    fun setVideoURI(uri: Uri) {
        this.uri = uri

        seekWhenPrepared = 0

        openVideo()

        requestLayout()
    }

    /**
     * Sets which type of audio focus will be requested during the playback, or configures playback
     * to not request audio focus. Valid values for focus requests are
     * [AudioManager.AUDIOFOCUS_GAIN], [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT],
     * [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK], and
     * [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE]. Or use
     * [AudioManager.AUDIOFOCUS_NONE] to express that audio focus should not be
     * requested when playback starts. You can for instance use this when playing a silent animation
     * through this class, and you don't want to affect other audio applications playing in the
     * background.
     * @param focusGain the type of audio focus gain that will be requested, or
     * [AudioManager.AUDIOFOCUS_NONE] to disable the use audio focus during playback.
     */
    fun setAudioFocusRequest(focusGain: Int) {
        if (focusGain != AudioManager.AUDIOFOCUS_NONE
            && focusGain != AudioManager.AUDIOFOCUS_GAIN
            && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
        ) {
            throw IllegalArgumentException("Illegal audio focus type $focusGain")
        }
        audioFocusType = focusGain
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentState = STATE_IDLE
        targetState = STATE_IDLE
        audioManager.abandonAudioFocus(null)
    }

    private fun openVideo() {
        if (uri == null || surfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false)

        if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            audioManager.requestAudioFocus(null, AudioManager.AUDIOFOCUS_GAIN, 0)
        }

        try {
            mediaPlayer = MediaPlayer()

            if (audioSession != 0) {
                mediaPlayer?.audioSessionId = audioSession
            } else {
                mediaPlayer?.run {
                    audioSession = audioSessionId
                }
            }

            mediaPlayer?.run {
                setOnPreparedListener(preparedListener)
                setOnVideoSizeChangedListener(sizeChangedListener)
                setOnCompletionListener(completionListener)
                setOnErrorListener(errorListener)
                setOnInfoListener(infoListener)
                setOnBufferingUpdateListener(bufferingUpdateListener)
                bufferPercentage = 0
                setDataSource(context, uri)
                setDisplay(surfaceHolder)
                setAudioAttributes(audioAttributes)
                setWakeMode(context, PowerManager.SCREEN_BRIGHT_WAKE_LOCK)
               // setScreenOnWhilePlaying(true)
                prepareAsync()
            }
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            currentState = STATE_PREPARING
        } catch (ex: IOException) {
            LogUtils.e(TAG, "Unable to open content: $uri")
            currentState = STATE_ERROR
            targetState = STATE_ERROR
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        } catch (ex: IllegalArgumentException) {
            LogUtils.e(TAG, "Unable to open content: $uri")
            currentState = STATE_ERROR
            targetState = STATE_ERROR
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        } finally {

        }
    }

    fun setMediaController(controller: MediaController) {
        mediaController?.hide()
        mediaController = controller
        mediaController?.isEnabled = isInPlaybackState()
    }

    private val sizeChangedListener: MediaPlayer.OnVideoSizeChangedListener =
        MediaPlayer.OnVideoSizeChangedListener { mp, _, _ ->
            videoWidth = mp.videoWidth
            videoHeight = mp.videoHeight
            if (videoWidth != 0 && videoHeight != 0) {
                holder.setFixedSize(videoWidth, videoHeight)
                requestLayout()
            }
        }

    private val preparedListener: MediaPlayer.OnPreparedListener =
        MediaPlayer.OnPreparedListener { mp ->
            currentState = STATE_PREPARED

            onPreparedListener?.onPrepared(mediaPlayer)

            mediaController?.run {
                isEnabled = true

                setLoadingIndicator(false)
            }
            videoWidth = mp.videoWidth
            videoHeight = mp.videoHeight

            val seekToPosition =
                seekWhenPrepared  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition)
            }
            if (videoWidth != 0 && videoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                holder.setFixedSize(videoWidth, videoHeight)
                if (surfaceWidth == videoWidth && surfaceHeight == videoHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (targetState == STATE_PLAYING) {
                        start()

                        mediaController?.show()
                    } else if (!isPlaying && (seekToPosition != 0 || currentPosition > 0)) {
                        // Show the media controls when we're paused into a video and make 'em stick.
                        mediaController?.show(0)
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (targetState == STATE_PLAYING) {
                    start()
                }
            }
        }

    private val completionListener = MediaPlayer.OnCompletionListener {
        currentState = STATE_PLAYBACK_COMPLETED
        targetState = STATE_PLAYBACK_COMPLETED

        mediaController?.hide()

        onCompletionListener?.onCompletion(mediaPlayer)

        if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            audioManager.abandonAudioFocus(null)
        }
    }

    private val infoListener = MediaPlayer.OnInfoListener { mp, arg1, arg2 ->
        onInfoListener?.onInfo(mp, arg1, arg2)
        true
    }

    private val errorListener =
        MediaPlayer.OnErrorListener { mp, framework_err, impl_err ->
            LogUtils.e(TAG, "Error: $framework_err,$impl_err")

            currentState = STATE_ERROR
            targetState = STATE_ERROR

            mediaController?.hide()

            /* If an error handler has been supplied, use it and finish. */
            onErrorListener?.run {
                return@run onError(mp, framework_err, impl_err)
            }
            true
        }

    private val bufferingUpdateListener =
        MediaPlayer.OnBufferingUpdateListener { mp, percent -> bufferPercentage = percent }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    fun setOnPreparedListener(l: MediaPlayer.OnPreparedListener) {
        onPreparedListener = l
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener) {
        onCompletionListener = l
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    fun setOnErrorListener(l: MediaPlayer.OnErrorListener) {
        onErrorListener = l
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    fun setOnInfoListener(l: MediaPlayer.OnInfoListener) {
        onInfoListener = l
    }

    /*
     * release the media player in any state
     */
    private fun release(clearTargetState: Boolean) {
        mediaPlayer?.run {
            reset()
            release()
            mediaPlayer = null
            currentState = STATE_IDLE
            if (clearTargetState) {
                targetState = STATE_IDLE
            }
            if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
                audioManager.abandonAudioFocus(null)
            }
        }
    }

    override fun start() {
        if (isInPlaybackState()) {
            mediaPlayer?.start()
            currentState = STATE_PLAYING
        }
        targetState = STATE_PLAYING
    }

    override fun pause() {
        if (isInPlaybackState()) {
            mediaPlayer?.run {
                if (isPlaying) {
                    pause()
                    currentState = STATE_PAUSED
                }
            }
        }
        targetState = STATE_PAUSED
    }

    fun suspend() {
        release(false)
    }

    fun resume() {
        openVideo()
    }

    private fun isInPlaybackState(): Boolean {
        return mediaPlayer != null && currentState != STATE_ERROR && currentState != STATE_IDLE
                && currentState != STATE_PREPARING
    }

    override var duration: Int = 0
        get() = if (isInPlaybackState()) {
            mediaPlayer!!.duration
        } else -1

    override var currentPosition: Int = 0
        get() = if (isInPlaybackState()) {
            mediaPlayer!!.currentPosition
        } else {
            0
        }
    override var isPlaying: Boolean = false
        get() = isInPlaybackState() && mediaPlayer!!.isPlaying

    override var bufferPercentage: Int = 0

    override var audioSessionId: Int = 0
        get() = if (audioSession == 0) {
            val foo = MediaPlayer()
            val sessionId = foo.audioSessionId
            foo.release()
            sessionId
        } else {
            audioSession
        }

    override fun seekTo(position: Int) {
        seekWhenPrepared = if (isInPlaybackState()) {
            mediaPlayer?.seekTo(position)
            0
        } else {
            position
        }
    }

    companion object {
        private const val TAG = "VideoView"

        // all possible internal states
        private const val STATE_ERROR = -1
        private const val STATE_IDLE = 0
        private const val STATE_PREPARING = 1
        private const val STATE_PREPARED = 2
        private const val STATE_PLAYING = 3
        private const val STATE_PAUSED = 4
        private const val STATE_PLAYBACK_COMPLETED = 5
    }
}
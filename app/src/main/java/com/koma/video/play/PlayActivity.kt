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

import android.content.ContentUris
import android.media.AudioManager
import android.media.MediaPlayer
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.koma.video.R
import com.koma.video.VideoApplication
import com.koma.video.base.BaseActivity
import com.koma.video.util.LogUtils
import kotlinx.android.synthetic.main.media_controller.*
import kotlinx.android.synthetic.main.play_activity.*
import javax.inject.Inject

class PlayActivity : BaseActivity(), PlayContract.View, GestureListener {
    @Inject
    lateinit var playPresenter: PlayPresenter

    override lateinit var presenter: PlayContract.Presenter

    private lateinit var gestureDetector: GestureDetector

    override fun onPermissionGranted() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // control volume
        volumeControlStream = AudioManager.STREAM_MUSIC

        val mediaId = intent.getLongExtra(KEY_MEDIA_ID, -1)

        // inject presenter
        DaggerPlayComponent.builder()
            .videoRepositoryComponent((application as VideoApplication).videoRepositoryComponent)
            .playPresenterModule(PlayPresenterModule(this))
            .build()
            .inject(this)

        // load title
        presenter.loadTitle(mediaId)

        // initial gesture processor
        gestureDetector = GestureDetector(this, GestureProcessor(this).also {
            it.setOnGestureListener(this)
        })

        video_view.setVideoURI(
            ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaId
            )
        )
        video_view.setMediaController(media_controller)
        video_view.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            finish()
        })
        media_controller.setMediaPlayer(video_view)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            showSystemUi(false)
        }
    }

    fun showSystemUi(visible: Boolean) {
        var flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        if (!visible) {
            flag = flag or (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
        window.decorView.systemUiVisibility = flag
    }

    override fun onStart() {
        super.onStart()

        LogUtils.d(TAG, "onStart")

        // play video
        video_view.start()
    }

    override fun setLoadingIndicator(active: Boolean) {
        media_controller.setLoadingIndicator(active)
    }

    override fun showTitle(title: String) {
        toolbar.title = title
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onSingleTapConfirmed(): Boolean {
        with(media_controller) {
            if (isShowing()) {
                hide()
            } else {
                show()
            }
        }

        return true
    }

    override fun onDoubleTap(): Boolean {
        media_controller.doPauseResume()

        return true
    }

    override fun onBrightnessChanged(percent: Int) {
        media_controller.removeCallbacks(fadeOut)

        card_indicator.visibility = View.VISIBLE
        tv_indicator.text = getString(R.string.percent_gesture_indicator, percent)
        tv_indicator.setCompoundDrawablesWithIntrinsicBounds(
            null,
            getDrawable(R.drawable.ic_brightness),
            null,
            null
        )

        media_controller.postDelayed(fadeOut, 1500)
    }

    override fun onVolumeChanged(percent: Int) {
        media_controller.removeCallbacks(fadeOut)

        card_indicator.visibility = View.VISIBLE
        tv_indicator.text = getString(R.string.percent_gesture_indicator, percent)
        tv_indicator.setCompoundDrawablesWithIntrinsicBounds(
            null,
            getDrawable(R.drawable.ic_volume_up),
            null,
            null
        )

        media_controller.postDelayed(fadeOut, 1500)
    }

    private val fadeOut = Runnable { card_indicator.visibility = View.GONE }

    override fun onProgressChanged() {
        media_controller.removeCallbacks(fadeOut)

        media_controller.postDelayed(fadeOut, 1500)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getLayoutId(): Int = R.layout.play_activity

    companion object {
        private const val TAG = "PlayActivity"

        const val KEY_MEDIA_ID = "key_media_id"
    }
}
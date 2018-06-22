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
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.koma.video.R
import com.koma.video.base.BaseFragment
import com.koma.video.util.LogUtils
import kotlinx.android.synthetic.main.play_fragment.*

class PlayFragment : BaseFragment(), PlayContract.View {
    override lateinit var presenter: PlayContract.Presenter

    override var isActive: Boolean = false
        get() = isAdded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        media_controller.setMediaPlayer(video_view)
        video_view.setMediaController(media_controller)

        arguments?.run {
            video_view.setVideoURI(
                ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    getLong(PlayActivity.KEY_MEDIA_ID)
                )
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LogUtils.i(TAG, "onActivityCreated")

        video_view.start()
    }

    override fun setLoadingIndicator(active: Boolean) {

    }

    override fun getLayoutId(): Int = R.layout.play_fragment

    companion object {
        private const val TAG = "PlayFragment"

        fun newInstance(mediaId: Long): PlayFragment {
            val fragment = PlayFragment()
            val bundle = Bundle()
            bundle.putLong(PlayActivity.KEY_MEDIA_ID, mediaId)
            fragment.arguments = bundle
            return fragment
        }
    }
}
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

import com.koma.video.R
import com.koma.video.VideoApplication
import com.koma.video.base.BaseActivity
import com.koma.video.util.LogUtils
import javax.inject.Inject

class PlayActivity : BaseActivity() {
    @Inject
    lateinit var presenter: PlayPresenter

    override fun onPermissionGranted() {
        val mediaId = intent.getLongExtra(KEY_MEDIA_ID, -1)
        val fragment = supportFragmentManager.findFragmentById(R.id.content_main) as PlayFragment?
                ?: PlayFragment.newInstance(mediaId).also {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.content_main, it)
                        .commit()
                }

        DaggerPlayComponent.builder()
            .videoRepositoryComponent((application as VideoApplication).videoRepositoryComponent)
            .playPresenterModule(
                PlayPresenterModule(
                    fragment,
                    mediaId.toInt()
                )
            )
            .build()
            .inject(this)
    }

    override fun getLayoutId(): Int = R.layout.play_activity

    companion object {
        const val KEY_MEDIA_ID = "key_media_id"
    }
}
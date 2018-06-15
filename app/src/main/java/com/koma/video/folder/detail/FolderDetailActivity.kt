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
package com.koma.video.folder.detail

import android.view.MenuItem
import com.koma.video.R
import com.koma.video.VideoApplication
import com.koma.video.base.BaseActivity
import kotlinx.android.synthetic.main.folder_detail_activity.*
import javax.inject.Inject

class FolderDetailActivity : BaseActivity() {
    @Inject
    lateinit var presenter: FolderDetailPresenter

    override fun getLayoutId(): Int = R.layout.folder_detail_activity

    override fun onPermissionGranted() {
        toolbar.title = intent.getStringExtra(BUCKET_NAME)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            this.setDisplayHomeAsUpEnabled(true)
        }

        val fragment =
            supportFragmentManager.findFragmentById(R.id.content_main) as FolderDetailFragment?
                    ?: FolderDetailFragment.newInstance().also {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.content_main, it).commit()
                    }

        DaggerFolderDetailComponent.builder()
            .videoRepositoryComponent(
                (application as VideoApplication).videoRepositoryComponent
            )
            .folderDetailPresenterModule(
                FolderDetailPresenterModule(
                    fragment,
                    intent.getIntExtra(BUCKET_ID, -1)
                )
            )
            .build()
            .inject(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val BUCKET_ID = "bucket_id"
        const val BUCKET_NAME = "bucket_name"
    }
}
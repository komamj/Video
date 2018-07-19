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
package com.koma.video.search

import android.support.v7.widget.SearchView
import android.view.MenuItem
import com.koma.video.R
import com.koma.video.VideoApplication
import com.koma.video.base.BaseActivity
import kotlinx.android.synthetic.main.search_activity.*
import javax.inject.Inject

class SearchActivity : BaseActivity() {
    @Inject
    lateinit var presenter: SearchPresenter

    override fun onPermissionGranted() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        search_view.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.run {
                        if (!isEmpty()) {
                            trim()

                            presenter.loadVideoEntries(this)
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            }
        )

        val fragment = supportFragmentManager.findFragmentById(R.id.content_main) as SearchFragment?
                ?: SearchFragment.newInstance().also {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.content_main, it).commit()
                }

        DaggerSearchComponent.builder()
            .videoRepositoryComponent(
                (application as VideoApplication).videoRepositoryComponent
            )
            .searchModule(SearchModule(fragment))
            .build()
            .inject(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getLayoutId(): Int = R.layout.search_activity
}
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
package com.koma.video.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import com.koma.video.R
import com.koma.video.VideosFragment
import com.koma.video.base.BaseActivity
import com.koma.video.folder.FoldersFragment
import com.koma.video.search.SearchActivity
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)

        super.onCreate(savedInstanceState)
    }

    override fun onPermissionGranted() {
        setSupportActionBar(toolbar)

        app_bar.post({
            val valueHeightAnimator = ValueAnimator
                .ofInt(app_bar.height, getAppBarHeight())
            valueHeightAnimator.addUpdateListener({
                val lp = app_bar.layoutParams
                lp.height = it.animatedValue as Int
                app_bar.layoutParams = lp
            })

            valueHeightAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    runnable.run()
                }
            })

            valueHeightAnimator.start()
        })
    }

    private val runnable = Runnable {
        view_pager.currentItem = 0
        view_pager.offscreenPageLimit = 1
        view_pager.adapter = MainAdapter(this, supportFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }

    private fun getAppBarHeight(): Int {
        val tv = TypedValue()
        theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        val actionBarHeight = TypedValue.complexToDimensionPixelSize(
            tv.data,
            resources.displayMetrics
        )
        return actionBarHeight + tab_layout.height
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_search -> {
                startActivity(
                    Intent(
                        this,
                        SearchActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getLayoutId(): Int = R.layout.main_activity

    private class MainAdapter(
        private val context: Context,
        fragmentManager: FragmentManager
    ) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> FoldersFragment.newInstance()
                else -> VideosFragment.newInstance()
            }
        }

        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                1 -> context.getString(R.string.folder_page_title)
                else -> context.getString(R.string.video_page_title)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

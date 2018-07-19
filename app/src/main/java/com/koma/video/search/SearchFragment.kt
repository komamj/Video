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

import android.os.Bundle
import com.koma.video.R
import com.koma.video.base.BaseFragment
import com.koma.video.data.enities.VideoEntry
import com.koma.video.util.LogUtils
import kotlinx.android.synthetic.main.search_fragment.*

class SearchFragment : BaseFragment(), SearchContract.View {
    override lateinit var presenter: SearchContract.Presenter

    private lateinit var adapter: SearchAdapter

    override var isActive: Boolean = false
        get() = isAdded

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LogUtils.d(TAG, "onActivityCreated")

        with(recycler_view) {
            layoutManager = android.support.v7.widget.LinearLayoutManager(context).also {
                it.orientation = android.support.v7.widget.LinearLayoutManager.VERTICAL
            }
            addItemDecoration(com.koma.video.widget.VideosItemDecoration(context))
            setHasFixedSize(true)
            // todo initial adapter
        }

        adapter = SearchAdapter()
    }

    override fun setLoadingIndicator(active: Boolean) {
        with(progress_bar) {
            if (active) {
                show()
            } else {
                hide()
            }
        }
    }

    override fun showVideoEntries(entries: List<VideoEntry>) {
    }

    override fun getLayoutId(): Int = R.layout.search_fragment

    companion object {
        private const val TAG = "SearchFragment"

        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}
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

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import com.koma.video.R
import com.koma.video.VideosAdapter
import com.koma.video.base.BaseFragment
import com.koma.video.data.enities.VideoEntry
import com.koma.video.util.LogUtils
import com.koma.video.widget.ItemAnimatorFactory
import com.koma.video.widget.VideosItemDecoration
import kotlinx.android.synthetic.main.base_fragment.*

class FolderDetailFragment : BaseFragment(), FolderDetailContract.View {
    override lateinit var presenter: FolderDetailContract.Presenter

    override var isActive: Boolean = false
        get() = isAdded

    private lateinit var videosAdapter: VideosAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LogUtils.d(TAG, "onActivityCreated")

        with(refresh_layout) {
            setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark)
            )
            setOnRefreshListener({
                presenter.subscribe()
            })
        }

        with(recycler_view) {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.VERTICAL
            }
            itemAnimator = ItemAnimatorFactory.slidein()
            addItemDecoration(VideosItemDecoration(context))
            setHasFixedSize(true)
            videosAdapter = VideosAdapter(context)
            adapter = videosAdapter
        }

        presenter.subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        LogUtils.d(TAG, "onDestroyView")

        presenter.unSubscribe()
    }

    override fun setLoadingIndicator(active: Boolean) {
        with(refresh_layout) {
            post {
                isRefreshing = active
            }
        }
    }

    override fun setEmptyIndicator(active: Boolean) {

    }

    override fun showVideoEntries(entries: List<VideoEntry>) {
        videosAdapter.submitList(entries)
    }

    override fun getLayoutId(): Int = R.layout.base_fragment

    companion object {
        private const val TAG = "FolderDetailFragment"

        fun newInstance(): FolderDetailFragment {
            return FolderDetailFragment()
        }
    }
}
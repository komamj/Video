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
package com.koma.video.folder

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.koma.video.R
import com.koma.video.VideoApplication
import com.koma.video.base.BaseFragment
import com.koma.video.data.enities.BucketEntry
import com.koma.video.util.LogUtils
import com.koma.video.widget.ItemAnimatorFactory
import com.koma.video.widget.VideosItemDecoration
import kotlinx.android.synthetic.main.base_fragment.*
import javax.inject.Inject

class FoldersFragment : BaseFragment(), FoldersContract.View {
    @Inject
    lateinit var foldersPresenter: FoldersPresenter

    override lateinit var presenter: FoldersContract.Presenter

    override var isActive: Boolean = false
        get() = isAdded

    private lateinit var foldersAdapter: FoldersAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        DaggerFoldersComponent.builder()
                .videoRepositoryComponent(
                        ((context as AppCompatActivity).application as VideoApplication)
                                .videoRepositoryComponent
                )
                .foldersPresenterModule(FoldersPresenterModule(this))
                .build()
                .inject(this)

        with(refresh_layout) {
            setColorSchemeColors(
                    ContextCompat.getColor(context, R.color.colorPrimary),
                    ContextCompat.getColor(context, R.color.colorAccent),
                    ContextCompat.getColor(context, R.color.colorPrimaryDark)
            )
            this.setOnRefreshListener {
                presenter.loadBucketEntries()
            }
        }

        with(recycler_view) {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.VERTICAL
            }
            itemAnimator = ItemAnimatorFactory.slidein()
            addItemDecoration(VideosItemDecoration(context))
            setHasFixedSize(true)
            foldersAdapter = FoldersAdapter(context)
            adapter = foldersAdapter
        }

        presenter.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()

        LogUtils.d(TAG, "onDestroy")

        presenter.unSubscribe()
    }

    override fun showBucketEntries(entries: List<BucketEntry>) {
        foldersAdapter.submitList(entries)
    }

    override fun setLoadingIndicator(active: Boolean) {
        with(refresh_layout) {
            post {
                isRefreshing = active
            }
        }
    }

    override fun setEmptyIndicator(active: Boolean) {
        super.showEmpty(active)
    }

    override fun getLayoutId(): Int = R.layout.base_fragment

    companion object {
        private const val TAG = "FoldersFragment"

        fun newInstance(): FoldersFragment {
            return FoldersFragment()
        }
    }
}
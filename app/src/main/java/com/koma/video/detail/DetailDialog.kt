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
package com.koma.video.detail

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.koma.video.R
import com.koma.video.VideoApplication
import com.koma.video.data.enities.VideoEntryDetail
import kotlinx.android.synthetic.main.detail_dialog.*
import javax.inject.Inject

class DetailDialog : BottomSheetDialogFragment(), DetailContract.View {
    override lateinit var presenter: DetailContract.Presenter

    override var isActive: Boolean = false
        get() = isAdded

    @Inject
    lateinit var detailPresenter: DetailPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.detail_dialog, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        DaggerDetailComponent.builder()
            .videoRepositoryComponent(
                ((context as AppCompatActivity).application as VideoApplication)
                    .videoRepositoryComponent
            )
            .detailPresenterModule(DetailPresenterModule(this))
            .build()
            .inject(this)

        arguments?.run {
            presenter.loadVideoEntryDetail(getLong(MEDIA_ID))
        }
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

    override fun showVideoEntryDetail(videoEntryDetail: VideoEntryDetail) {
        tv_title_description.text = videoEntryDetail.title
        tv_duration_description.text = videoEntryDetail.formatDuration
        context?.run {
            tv_date_taken.text = getString(R.string.date_taken, videoEntryDetail.formatDateTaken())
            tv_format.text = getString(R.string.format, videoEntryDetail.format)
            tv_size.text = getString(R.string.size, videoEntryDetail.formatSize(this))
            tv_path.text = getString(R.string.path, videoEntryDetail.path)
        }
        group.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "detail"

        const val MEDIA_ID = "media_id"

        fun show(fragmentManager: FragmentManager, mediaId: Long) {
            val dialog = DetailDialog()
            val bundle = Bundle()
            bundle.putLong(MEDIA_ID, mediaId)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }
}
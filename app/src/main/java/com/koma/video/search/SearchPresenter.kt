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

import com.koma.video.data.source.VideoRepository
import com.koma.video.util.LogUtils
import io.reactivex.disposables.CompositeDisposable

class SearchPresenter constructor(
    private val view: SearchContract.View,
    private val repository: VideoRepository
) : SearchContract.Presenter {
    private val disposables: CompositeDisposable

    init {
        view.presenter = this

        disposables = CompositeDisposable()
    }

    override fun subscribe() {
        LogUtils.d(TAG, "subscribe")
    }

    override fun unSubscribe() {
        LogUtils.d(TAG, "unSubscribe")

        disposables.clear()
    }


    override fun loadVideoEntries(ketword: String) {
    }

    companion object {
        private const val TAG = "SearchPresenter"
    }
}
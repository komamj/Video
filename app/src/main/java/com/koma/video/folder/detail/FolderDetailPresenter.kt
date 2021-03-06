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

import com.koma.video.data.source.VideoRepository
import com.koma.video.util.LogUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FolderDetailPresenter @Inject constructor(
    private val view: FolderDetailContract.View,
    private val bucketId: Int,
    private val repository: VideoRepository
) : FolderDetailContract.Presenter {

    private val disposables by lazy {
        CompositeDisposable()
    }

    init {
        view.presenter = this
    }

    override fun subscribe() {
        LogUtils.d(TAG, "subscribe")

        loadVideoEntries(bucketId)
    }

    override fun unSubscribe() {
        LogUtils.d(TAG, "unSubscribe")

        disposables.clear()
    }

    override fun loadVideoEntries(bucketId: Int) {
        if (view.isActive) {
            view.setLoadingIndicator(true)
        }

        val disposable = repository.getVideoEntries(bucketId)
            .delay(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    with(view) {
                        if (isActive) {
                            showVideoEntries(it)

                            setEmptyIndicator(it.isEmpty())
                        }
                    }
                }, onError = {
                    LogUtils.e(TAG, "loadVideoEntries error : " + it.message)
                }, onComplete = {
                    with(view) {
                        if (isActive) {
                            setLoadingIndicator(false)
                        }
                    }
                }
            )
        disposables.add(disposable)
    }

    companion object {
        private const val TAG = "FolderDetailPresenter"
    }
}
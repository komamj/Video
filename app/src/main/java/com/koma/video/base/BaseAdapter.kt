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
package com.koma.video.base

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import com.koma.video.util.LogUtils
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder> constructor(
    protected val context: Context
) : RecyclerView.Adapter<VH>() {
    private val disposables = CompositeDisposable()

    protected val data = mutableListOf<T>()

    fun submitList(newList: List<T>?) {
        if (newList === data) {
            // nothing to do
            return
        }

        // fast simple remove all
        if (newList == null) {
            val countRemoved = data.size
            data.clear()
            notifyItemRangeRemoved(0, countRemoved)
            return
        }

        disposables.clear()

        val disposable = Flowable.create<DiffUtil.DiffResult>({
            val oldList = data

            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return oldList.size
                }

                override fun getNewListSize(): Int {
                    return newList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return this@BaseAdapter.areItemsTheSame(
                        oldList[oldItemPosition],
                        newList[newItemPosition]
                    )
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return this@BaseAdapter.areContentsTheSame(
                        oldList[oldItemPosition],
                        newList[newItemPosition]
                    )
                }

                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                    return this@BaseAdapter.getChangePayload(
                        oldList[oldItemPosition],
                        newList[newItemPosition]
                    )
                }
            })
            it.onNext(result)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    data.clear()
                    data.addAll(newList)
                    it.dispatchUpdatesTo(this)
                },
                onError = {
                    LogUtils.e("BaseAdapter", "diffutil error : ${it.message}")
                },
                onComplete = {

                })
        disposables.add(disposable)
    }

    abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    abstract fun getChangePayload(oldItem: T, newItem: T): Any?

    protected fun getItem(position: Int): T {
        return data[position]
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)

        LogUtils.d(TAG, "onViewDetachedFromWindow")

        disposables.clear()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    companion object {
        private const val TAG = "BaseAdapter"
    }
}
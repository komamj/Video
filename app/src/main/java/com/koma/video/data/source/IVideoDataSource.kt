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
package com.koma.video.data.source

import com.koma.video.data.enities.BucketEntry
import com.koma.video.data.enities.VideoEntry
import io.reactivex.Flowable

interface IVideoDataSource {
    fun getVideoEntries(): Flowable<List<VideoEntry>>

    fun getBucketEntries(): Flowable<List<BucketEntry>>

    fun getVideoEntries(bucketId: Int): Flowable<List<VideoEntry>>
}
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
package com.koma.video.util

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.koma.video.data.enities.BucketEntry
import java.io.InputStream

class VideoThumbLoader constructor(private val context: Context) :
    ModelLoader<BucketEntry, InputStream> {
    override fun buildLoadData(
        model: BucketEntry,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(ObjectKey(model), VideoThumbFetcher(context, model))

    }

    override fun handles(model: BucketEntry): Boolean = true

    class Factory(private val context: Context) : ModelLoaderFactory<BucketEntry, InputStream> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<BucketEntry, InputStream> {
            return VideoThumbLoader(context)
        }

        override fun teardown() {
            // Do nothing.
        }
    }
}
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
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.koma.video.data.enities.BucketEntry
import java.io.InputStream

@GlideModule
class VideoGlideModule : AppGlideModule() {
    override fun registerComponents(
        context: Context, glide: Glide,
        registry: Registry
    ) {
        registry.append(
            BucketEntry::class.java,
            InputStream::class.java,
            VideoThumbLoader.Factory(context)
        )
    }
}
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
package com.koma.video

import android.app.Application
import com.koma.video.data.source.DaggerVideoRepositoryComponent
import com.koma.video.data.source.VideoRepositoryComponent
import com.koma.video.data.source.VideoRepositoryModule
import com.squareup.leakcanary.LeakCanary
import javax.inject.Inject

class VideoApplication : Application() {
    @Inject
    lateinit var videoRepositoryComponent: VideoRepositoryComponent

    override fun onCreate() {
        super.onCreate()

        videoRepositoryComponent = DaggerVideoRepositoryComponent.builder()
            .applicationModule(ApplicationModule(applicationContext))
            .videoRepositoryModule(VideoRepositoryModule())
            .build()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        LeakCanary.install(this)
    }
}
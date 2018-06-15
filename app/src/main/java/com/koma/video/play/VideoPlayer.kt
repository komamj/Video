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
package com.koma.video.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

class VideoPlayer constructor(private val context: Context) {
    init {

    }

    // We want to pause when the headset is unplugged.
    private inner class AudioBecomingNoisyReceiver : BroadcastReceiver() {
        fun register() {
            context.registerReceiver(
                this,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            )
        }

        fun unregister() {
            context.unregisterReceiver(this)
        }

        override
        fun onReceive(context: Context, intent: Intent) {

        }
    }

    companion object {
        private const val TAG = "VideoPlayer"
    }
}
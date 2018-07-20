/*
 * Copyright 2018 Koma
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
package com.koma.video.widget

import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorCompat
import android.support.v7.widget.RecyclerView
import android.view.animation.Interpolator

class SlideInUpDelayedAnimator(private val interpolator: Interpolator) : BaseItemAnimator() {
    override fun preAnimateAdd(holder: RecyclerView.ViewHolder) {
        with(holder.itemView) {
            translationY = height.toFloat()
            alpha = 0f
        }
    }

    override fun onAnimatedAdd(holder: RecyclerView.ViewHolder): ViewPropertyAnimatorCompat {
        return ViewCompat.animate(holder.itemView)
            .translationY(0f)
            .setInterpolator(interpolator)
            .setStartDelay((200 * holder.layoutPosition).toLong())
    }
}
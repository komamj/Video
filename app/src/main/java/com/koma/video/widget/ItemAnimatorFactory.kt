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

import android.support.v7.widget.RecyclerView
import android.view.animation.DecelerateInterpolator

object ItemAnimatorFactory {
    fun slidein(): RecyclerView.ItemAnimator {
        val animator = SlideInUpDelayedAnimator(DecelerateInterpolator(1.2f))
        animator.addDuration = 600
        return animator
    }
}

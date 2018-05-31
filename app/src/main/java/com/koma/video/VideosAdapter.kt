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

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.koma.video.base.BaseAdapter
import com.koma.video.data.enities.VideoEntry
import com.koma.video.util.GlideApp

class VideosAdapter(context: Context) : BaseAdapter<VideoEntry, VideosAdapter.VideosVH>(
    context = context,
    diffCallback = object : DiffUtil.ItemCallback<VideoEntry>() {
        override fun areItemsTheSame(oldItem: VideoEntry, newItem: VideoEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VideoEntry, newItem: VideoEntry): Boolean {
            return oldItem.equals(newItem)
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosVH {
        return VideosVH(
            LayoutInflater.from(context).inflate(R.layout.video_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VideosVH, position: Int) {
        val videoEntry = getItem(position)
        GlideApp.with(context)
            .asBitmap()
            .placeholder(ColorDrawable(Color.GRAY))
            .thumbnail(0.1f)
            .load(videoEntry.uri)
            .into(holder.image)
        holder.name.text = videoEntry.displayName
        holder.duration.text = videoEntry.formatDuration
    }

    class VideosVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val image: ImageView
        val duration: TextView
        val name: TextView

        init {
            itemView.setOnClickListener(this)
            image = itemView.findViewById(R.id.iv_video)
            name = itemView.findViewById(R.id.tv_name)
            duration = itemView.findViewById(R.id.tv_duration)
            (itemView.findViewById(R.id.iv_more) as ImageView).setOnClickListener(this)
        }


        override fun onClick(view: View) {
            when (view.id) {
                R.id.iv_more -> {
                    val popupMenu = PopupMenu(view.context, view)
                }
                else -> {

                }
            }
        }
    }
}
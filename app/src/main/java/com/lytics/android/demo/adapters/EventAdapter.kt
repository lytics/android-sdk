package com.lytics.android.demo.adapters

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lytics.android.demo.data.Event
import com.lytics.android.demo.databinding.ListItemEventBinding

class EventAdapter(
    context: Context,
    private val eventClickListener: (event: Event) -> Unit
) : ListAdapter<Event, RecyclerView.ViewHolder>(EventDiffCallback()) {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EventViewHolder(ListItemEventBinding.inflate(layoutInflater, parent, false), eventClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val event = getItem(position)
        (holder as EventViewHolder).bind(event)
    }

    class EventViewHolder(
        private val binding: ListItemEventBinding,
        private val eventClickListener: (event: Event) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.apply {
                eventName.text = event.artist
                eventLocation.text = event.location
                eventThumbnail.setImageResource(event.thumbnailImage)
                root.setOnClickListener {
                    eventClickListener(event)
                }
            }
        }
    }
}

private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}

class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceSize
            }
            bottom = spaceSize
        }
    }
}

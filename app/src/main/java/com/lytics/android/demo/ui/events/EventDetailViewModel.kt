package com.lytics.android.demo.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lytics.android.demo.data.Event
import com.lytics.android.demo.data.EventRepository

class EventDetailViewModel : ViewModel() {

    private val _event = MutableLiveData<Event?>().apply {
        value = null
    }
    val event: LiveData<Event?> = _event

    fun loadEvent(eventId: Long) {
        _event.value = EventRepository.getEvent(eventId)
    }
}

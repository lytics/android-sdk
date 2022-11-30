package com.lytics.android.demo.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lytics.android.demo.data.Event
import com.lytics.android.demo.data.EventRepository

class EventsViewModel : ViewModel() {

    private val _closestEvent = MutableLiveData<Event>().apply {
        value = EventRepository.getClosestEvent()
    }
    val closestEvent: LiveData<Event> = _closestEvent

    private val _popularEvents = MutableLiveData<List<Event>>().apply {
        value = EventRepository.getPopularEvents()
    }
    val popularEvents: LiveData<List<Event>> = _popularEvents
}
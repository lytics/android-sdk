package com.lytics.android.demo.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lytics.android.demo.R
import com.lytics.android.demo.adapters.EventAdapter
import com.lytics.android.demo.adapters.MarginItemDecoration
import com.lytics.android.demo.data.Event
import com.lytics.android.demo.databinding.FragmentEventsBinding
import com.lytics.android.demo.ui.events.EventDetailFragment.Companion.KEY_EVENT_ID

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val eventsViewModel = ViewModelProvider(this).get(EventsViewModel::class.java)

        _binding = FragmentEventsBinding.inflate(inflater, container, false)

        val eventAdapter = EventAdapter(requireContext()) { event ->
            goToEvent(event)
        }
        binding.popularEventsList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = eventAdapter
            addItemDecoration(MarginItemDecoration(resources.getDimensionPixelSize(R.dimen.margin)))
        }

        eventsViewModel.closestEvent.observe(viewLifecycleOwner) { event ->
            binding.closestEventImage.setImageResource(event.image)
            binding.closestEventName.text = event.artist
            binding.closestEventLocation.text = event.location
            binding.closestEventDate.text = "MAR 18"
            binding.closestEventImage.setOnClickListener {
                goToEvent(event)
            }
            binding.closestEventBuyButton.setOnClickListener {
                goToEvent(event)
            }
        }

        eventsViewModel.popularEvents.observe(viewLifecycleOwner) {
            eventAdapter.submitList(it)
        }

        return binding.root
    }

    private fun goToEvent(event: Event) {
        findNavController().navigate(R.id.action_events_to_event_detail, bundleOf(KEY_EVENT_ID to event.id))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

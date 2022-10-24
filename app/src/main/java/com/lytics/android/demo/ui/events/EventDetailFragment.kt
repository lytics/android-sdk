package com.lytics.android.demo.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lytics.android.demo.databinding.FragmentEventDetailBinding

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: EventDetailViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)

        val eventId = arguments?.getLong(KEY_EVENT_ID)
        eventId?.let { viewModel.loadEvent(it) }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event?.let {
                binding.eventDetails.text = event.details
                binding.eventDate.text = event.date
                binding.eventImage.setImageResource(event.image)
                binding.eventName.text = event.artist
                binding.eventLocation.text = event.location

                binding.eventBuyButton.setOnClickListener {

                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_EVENT_ID = "eventId"
    }
}

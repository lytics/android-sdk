package com.lytics.android.demo.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lytics.android.Lytics

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Getting Lytics profile"
    }
    val text: LiveData<String> = _text

    fun updateLyticsProfile() {
        _text.value = Lytics.currentUser?.serialize()?.toString(2)
    }
}
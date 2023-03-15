package com.lytics.android.demo.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lytics.android.Lytics
import com.lytics.android.LyticsUser
import kotlinx.coroutines.launch
import org.json.JSONObject

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Getting Lytics profile"
    }
    val text: LiveData<String> = _text

    fun updateLyticsProfile() {
        viewModelScope.launch {
            val userWithProfile = Lytics.getProfile() ?: Lytics.currentUser
            _text.value = userWithProfile?.let { user ->
                val json = user.serialize()
                user.profile?.let { profile ->
                    json.put("profile", JSONObject(profile))
                }
                json.toString(2)
            } ?: "error getting user"
        }
    }
}
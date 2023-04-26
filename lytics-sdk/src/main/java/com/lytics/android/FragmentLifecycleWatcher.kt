package com.lytics.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lytics.android.events.LyticsEvent

/**
 * Callbacks for fragment lifecycle events
 */
internal class FragmentLifecycleWatcher(private val lytics: ILytics): FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentResumed(fragmentManager: FragmentManager, fragment: Fragment) {
        lytics.logger?.debug("onFragmentResumed: ${fragment.javaClass.simpleName}")

        // mark when the fragment was resumed
        lytics.markLastInteractionTime()

        // if auto tracking screens, track fragment screen event
        if (lytics.configuration.autoTrackFragmentScreens) {
            lytics.screen(LyticsEvent(name=fragment.javaClass.simpleName))
        }
    }

    override fun onFragmentPaused(fragmentManager: FragmentManager, fragment: Fragment) {
        lytics.logger?.debug("onFragmentPaused: ${fragment.javaClass.simpleName}")

        // mark when the fragment is paused
        lytics.markLastInteractionTime()
    }
}
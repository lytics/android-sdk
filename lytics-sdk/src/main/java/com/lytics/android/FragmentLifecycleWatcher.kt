package com.lytics.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lytics.android.events.LyticsEvent

/**
 * Callbacks for fragment lifecycle events
 */
internal class FragmentLifecycleWatcher: FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentResumed(fragmentManager: FragmentManager, fragment: Fragment) {
        Lytics.logger?.debug("onFragmentResumed: ${fragment.javaClass.simpleName}")

        // mark when the fragment was resumed
        Lytics.markLastInteractionTime()

        // if auto tracking screens, track fragment screen event
        if (Lytics.configuration.autoTrackFragmentScreens) {
            Lytics.screen(LyticsEvent(name=fragment.javaClass.simpleName))
        }
    }

    override fun onFragmentPaused(fragmentManager: FragmentManager, fragment: Fragment) {
        Lytics.logger?.debug("onFragmentPaused: ${fragment.javaClass.simpleName}")

        // mark when the fragment is paused
        Lytics.markLastInteractionTime()
    }
}
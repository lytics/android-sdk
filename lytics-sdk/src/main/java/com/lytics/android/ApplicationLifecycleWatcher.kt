package com.lytics.android

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.FragmentActivity
import com.lytics.android.events.LyticsEvent
import java.util.concurrent.atomic.AtomicLong

/**
 * Callback for application's activity's lifecycle events.
 */
internal class ApplicationLifecycleWatcher(private val lastInteractionTimestamp: Long): ActivityLifecycleCallbacks  {

    private var currentActivityName: String? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Lytics.logger.debug("onActivityCreated: ${activity.javaClass.simpleName}")
        if (Lytics.configuration.autoTrackFragmentScreens) {
            (activity as? FragmentActivity)
                ?.supportFragmentManager
                ?.registerFragmentLifecycleCallbacks(FragmentLifecycleWatcher(), true)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        // no-op
    }

    override fun onActivityResumed(activity: Activity) {
        Lytics.logger.debug("onActivityResumed: ${activity.javaClass.simpleName}")

        // mark when the activity resumes
        Lytics.markLastInteractionTime()

        // if this is the first activity being opened, send app open event
        if (currentActivityName.isNullOrEmpty() && Lytics.configuration.autoTrackAppOpens) {
            Lytics.track(LyticsEvent(name = "App Open", properties = mapOf("activity" to activity.javaClass.simpleName)))
        }
        currentActivityName = activity.javaClass.simpleName

        // if auto tracking screens, send activity screen name
        if (Lytics.configuration.autoTrackActivityScreens) {
            Lytics.screen(LyticsEvent(name=activity.javaClass.simpleName))
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Lytics.logger.debug("onActivityPaused: ${activity.javaClass.simpleName}")
        // mark when the activity was paused
        Lytics.markLastInteractionTime()
    }

    override fun onActivityStopped(activity: Activity) {
        // no-op
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // no-op
    }

    override fun onActivityDestroyed(activity: Activity) {
        // no-op
    }
}
package com.lytics.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.events.LyticsEvent
import com.lytics.android.logging.AndroidLogger
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify


private class TestFragment : Fragment() {

}

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleWatcherTest {

    @Test
    fun test_onFragmentResumed_noTracking() {
        val mockLytics = mock<ILytics> {
            on { configuration } doReturn LyticsConfiguration("API_KEY")
        }
        val fragmentManager = mock<FragmentManager>()
        val fragment = TestFragment()

        val fragmentLifecycleWatcher = FragmentLifecycleWatcher(mockLytics)
        fragmentLifecycleWatcher.onFragmentResumed(fragmentManager, fragment)
        verify(mockLytics).markLastInteractionTime()
    }

    @Test
    fun test_onFragmentResumed_withTracking() {
        val mockLytics = mock<ILytics> {
            on { configuration } doReturn LyticsConfiguration("API_KEY", autoTrackFragmentScreens = true)
            on { logger } doReturn AndroidLogger
        }
        val fragmentManager = mock<FragmentManager>()
        val fragment = TestFragment()

        val fragmentLifecycleWatcher = FragmentLifecycleWatcher(mockLytics)
        fragmentLifecycleWatcher.onFragmentResumed(fragmentManager, fragment)
        verify(mockLytics).markLastInteractionTime()
        verify(mockLytics).screen(LyticsEvent(name = "TestFragment"))
    }

    @Test
    fun test_onFragmentPaused() {
        val mockLytics = mock<ILytics> {
            on { logger } doReturn AndroidLogger
        }
        val fragmentManager = mock<FragmentManager>()
        val fragment = TestFragment()

        val fragmentLifecycleWatcher = FragmentLifecycleWatcher(mockLytics)
        fragmentLifecycleWatcher.onFragmentPaused(fragmentManager, fragment)
        verify(mockLytics).markLastInteractionTime()
    }

}
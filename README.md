# Quick Start Guide


## Installation

1. Add the Lytics Android SDK library as a gradle dependency.

TBD.

2. Add permissions to your AndroidManifest.xml

Add the following permissions to your AndroidManifest.xml file:

`<uses-permission android:name="android.permission.INTERNET" />`

This permission is required to send events to the Lytics API.

`<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`

This permission is technically optional but highly recommended to allow the library to determine the best time to send events to the API.


## Initialize Lytics SDK

In the app's `Application` class, create a configuration for Lytics and initialize the SDK.

The minimum required for configuration is the [Lytics API token](https://learn.lytics.com/documentation/product/features/account-management/managing-api-tokens) and the name of the default stream.

Pass this configuration along with the Application Context to the Lytics SDK library to initialize it.

```kotlin
import android.app.Application
import com.lytics.android.Lytics
import com.lytics.android.LyticsConfiguration
import com.lytics.android.logging.LogLevel

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LyticsConfiguration(
            apiKey = "YOUR LYTICS API KEY",
        )
        Lytics.init(applicationContext, config)
    }
}
```

## Sending Data

Before data can be sent to the Lytics SDK, `Lytics.optIn()` needs to be trigger indicating the user is allowing data to be tracked.

### Identity Events

Tracking identity events provides an interface for updating the current users properties stored on device as well as emitting an identify event to the downstream collections API.

```kotlin
val email = "some@email.com"
Lytics.identify(LyticsIdentityEvent(name="login", identifiers = mapOf("email" to email)))
```

### Consent Events

Consent events provide an interface for configuring and emitting a special event that represents an app users explicit consent. This event does everything a normal event does in addition to providing a special payload for consent details at the discretion of the developer.

```kotlin
val termsAndConditions = true
Lytics.consent(LyticsConsentEvent(name = "android consent", consent = mapOf("terms" to termsAndConditions)))
```

### Track Custom Events

Track custom events provides an interface for configuring and emitting a custom event at the customers discretion throughout their application (e.g. made a purchase or logged in)

```kotlin
Lytics.track(
    LyticsEvent(
        name = "Buy Tickets",
        properties = mapOf("eventId" to event.id, "artist" to event.artist)
    )
)
```

### Screen Events

Screen events provide an interface for configuring and emitting a special event that represents a screen or page view. It should be seen as an extension of the track method

```kotlin
Lytics.screen(LyticsEvent(name="Dashboard"))
```

Lytics can also be configured to automatically track screens/page views of activities and/or fragments, depending on your application's architecture.

When `autoTrackActivityScreens` and/or `autoTrackFragmentScreens` are set to true in the Lytics configuration, when an activity and/or fragment is resumed, a screen event with the simple name of the activity or fragment class will be sent.

For example, when the EventDetailFragment is resumed, the following screen event would be triggered.

```kotlin
Lytics.screen(LyticsEvent(name="EventDetailFragment"))
```


## Advertising ID

To support collecting the Android Advertising ID, add the following to the application's gradle dependencies:

`implementation 'com.google.android.gms:play-services-ads-identifier:18.0.1'`

Additionally, declare a Google Play services normal permission in the manifest file as follows:

`<uses-permission android:name="com.google.android.gms.permission.AD_ID"/>`

After confirming with the user and getting their consent, enable Advertiser ID collection via `Lytics.enableIDFA()`.

The user's Android Advertising ID will be sent with each event's identifiers.

Note, the user can disable or change the Advertising ID via the Android system privacy settings.

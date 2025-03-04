# Android-StreamIngest-SDK

The streamingest SDK is a streaming solution based on the RTMP protocol.

## Requirements

- **IDE**: Android Studio Ladybug or later
- **minSdkVersion**: 24
- **targetSdkVersion**: 35
- **Kotlin Version**: 2.0.0 or later

## Integration

### In your settings.gradle file, `dependencyResolutionManagement` sections:
[Gets username and password](https://github.com/BlendVision/Android-StreamIngest-Samples/wiki/Android%E2%80%90StreamIngest%E2%80%90SDK-pull-credentials)
```kotlin
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      //-----add below------//
      maven { url = uri("https://jitpack.io") }
      maven {
         url = uri("https://maven.pkg.github.com/blendvision/Android-Packages")
         credentials {
            username = //TODO
            password = //TODO
         }
      }
      //------------------//
   }
}
```

### Add the dependencies for the Messaging SDK to your module's app-level Gradle file, normally app/build.gradle:

```kotlin
dependencies {
   implementation("com.blendvision.stream.ingest:streamingest:$latest_version")
}
```

## Usage

### 1. Setup

Add `configChanges` tag in your `AndroidManifest.xml` to prevent the activity from being recreated
during screen orientation changes.

```xml

<activity android:name=".MainActivity" android:configChanges="keyboardHidden|orientation|screenSize" android:exported="true">
   <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LAUNCHER" />
   </intent-filter>
</activity>
```

### 2. Integrate `StreamingestView` in Your Layout

This view provides a preview rendering from the camera.

```xml
<com.blendvision.stream.ingest.ui.presentation.view.StreamIngestView
    android:id="@+id/streamIngestView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
/>
```

### 3. Start streaming

Set up a stream state listener, and start publishing your stream.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val streamConfig = StreamConfig(licenseKey = "YOUR_LICENSE_KEY")

    //create stream ingest,and set validation listener
    StreamIngest.Factory(context, streamConfig).create(
        object : ValidationListener {
            override fun onValidationSuccess(streamIngest: StreamIngest) {
                //when validation success,it will return StreamIngest instance
                setupStreamIngestView(streamIngest)
            }

            override fun onValidationFailed(exception: Exception) {
                //validation failed
            }
        }
    )

}

private fun setupStreamIngestView(streamIngest: StreamIngest) {
    observeStreamStatus(streamIngest)
    streamIngest.setStreamQuality(StreamQuality.High)
    registerFilter(streamIngest)
    streamIngestView.streamIngest = streamIngest
}

private fun registerFilter(streamIngest: StreamIngest) {
    //set skin Smooth Filter intensity value between 0-100,and it also can be changed during streaming
    val skinSmoothFilter = BeautyFilter.SkinSmoothFilter()
    skinSmoothFilter.setIntensity(50)
    //register filter
    val filters = listOf(skinSmoothFilter)
    //if you want to unregister filter call below: `streamIngest.unregisterFilter(filters)`
    streamIngest.registerFilter(filters)
}

private fun observeStreamStatus(streamIngest: StreamIngest) {
    streamIngest.streamStatus.onEach { streamState ->
        when (streamState) {
            StreamState.INITIALIZED -> {
                //streamingest view has been initialized
            }

            StreamState.RTMP_SERVER_IS_CONNECTING -> {
                //connecting to rtmp server
            }

            StreamState.RTMP_SERVER_IS_CONNECT_SUCCESS -> {
                //connect success
            }

            StreamState.RTMP_SERVER_IS_DISCONNECT -> {
                //disconnect
            }
        }
    }.launchIn(lifecycleScope)

    streamIngest.error.onEach { streamIngestException ->
        when (streamIngestException) {
            is StreamIngestException.ConnectionFailed -> {
                //connection failed
            }
            is StreamIngestException.EncoderFailed -> {
                //encoder failed
            }
            is StreamIngestException.LicenseFailed -> {
                //license failed
            }
            is StreamIngestException.RtmpServerFailed -> {
                //rtmp server failed
            }
            is StreamIngestException.Unknown -> {
                //unknown error
            }
        }
    }.launchIn(lifecycleScope)

    streamIngest.streamInsightStatus.onEach { signal ->
        when (signal) {
            is StreamInsight.PREPARED -> {
                //signal is prepared
            }
            is StreamInsight.Fine -> {
                //signal is fine
            }
            is StreamInsight.Warning -> {
                //signal is warning
            }
            is StreamInsight.Bad -> {
                //signal is bad 
            }
        }
    }.launchIn(lifecycleScope)
    
}

```

Initiates the streaming process with the provided RTMP URL and stream key.
> Note: Before calling the method, please ensure `streamState` is `StreamState.INITIALIZED`, which means streamIngestView has been initialized and prepared.

```kotlin
streamIngest.startStream("YOUR_RTMP_URL", "YOUR_STREAM_KEY")
```

Stops the ongoing streaming process.

```kotlin
streamIngest.stopStream()
```

### 4. release stream

Please ensure to release the streamingest view to prevent memory leaks when the current page is
destroyed.

```kotlin
override fun onDestroy() {
   super.onDestroy()
   streamIngest.release()
   streamIngestView.streamIngest = null
}
```

#### If you want to know more about the APIs, please refer to the [APIs docs](https://blendvision.github.io/Android-StreamIngest-Samples/)

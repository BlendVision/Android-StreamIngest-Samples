# Android-StreamIngest-SDK

The streamingest SDK is a streaming solution based on the RTMP protocol.

## Requirements

- **IDE**: Android Studio 3.0 or later
- **minSdkVersion**: 24
- **targetSdkVersion**: 34
- **Kotlin Version**: 1.9.x or later

## Integration

### In your settings.gradle file, `dependencyResolutionManagement` sections:

```groovy
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      //-----add below------//
      maven { url = uri("https://jitpack.io") }
      maven {
         url = uri("https://maven.pkg.github.com/blendvision/Android-StreamIngest-Samples")
         credentials {
             username = "bv-github-access"
             password = "ghp_veo1hAnJoT4jv0RY@RzHSCLKMm7UFoD4PoWda" //Please remove "@" character from password
         }
      }
      //------------------//
   }
}
```

### Add the dependencies for the Messaging SDK to your module's app-level Gradle file, normally app/build.gradle:

```groovy
dependencies {
    implementation 'com.blendvision.stream.ingest:streamingest:2.0.2'
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
    streamIngest.setStreamQuality(StreamQuality.High())
    val filters = listOf(BeautyFilter.SkinSmoothFilter())
    //if you want to unregister filter call below: `streamIngest.unregisterFilter(filters)`
    streamIngest.registerFilter(filters)
    streamIngestView.streamIngest = streamIngest
}

private fun observeStreamStatus(streamIngest: StreamIngest) {
    streamIngest.streamStatus.onEach { streamState ->
        when (streamState) {
            StreamState.INITIALIZED -> {
                //initialized.
            }

            StreamState.CONNECT_STARTED -> {
                //connect stared.
            }

            StreamState.CONNECT_SUCCESS -> {
                //connect success
            }

            StreamState.DISCONNECT -> {
                //disconnect
            }
        }
    }.launchIn(lifecycleScope)

    streamIngest.error.onEach { error ->
        //error.message
    }.launchIn(lifecycleScope)
}

```

Initiates the streaming process with the provided rtmp url and stream key.

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

## Others StreamIngestView API

```kotlin

/**
 * Set the stream quality for the ongoing streaming process.
 *
 * @param streamQuality The desired quality for the streaming process.
 */
streamIngest.setStreamQuality(streamQuality: StreamQuality)

/**
 * Switches the camera for the ongoing streaming process.
 */
streamIngest.switchCamera()

/**
 * Mutes or unmutes the video stream based on the given flag.
 *
 * @param isMute True to mute the video, false to unmute.
 */
streamIngest.mutedVideo(isMute: Boolean)

/**
 * Mutes or unmutes the audio stream based on the given flag.
 *
 * @param isMute True to mute the audio, false to unmute.
 */
streamIngest.mutedAudio(isMute: Boolean)

/**
 * Initiates the streaming process with the provided RTMP URL.
 *
 * @param rtmpUrl The RTMP URL for streaming.
 */
streamIngest.startStream(rtmpUrl: String)

/**
 * Stops the ongoing streaming process.
 */
streamIngest.stopStream()

/**
 * Starts the camera preview for the ongoing streaming process.
 */
streamIngest.startCameraPreview()

/**
 * Stops the camera preview for the ongoing streaming process.
 */
streamIngest.stopCameraPreview()

/**
 * Register a list of beauty filters.
 *
 * @param beautyFilters The list of beauty filters to register.
 */
streamIngest.registerFilter(beautyFilters: List< BeautyFilter >)

/**
 * Unregister a list of beauty filters.
 *
 * @param beautyFilters The list of beauty filters to unregister.
 */
streamIngest.unregisterFilter(beautyFilters: List< BeautyFilter >)

/**
 * Called when the configuration of the view changes.
 */
streamIngest.onConfigChanged()

/**
 * Releases resources associated with the streaming process.
 */
streamIngest.release()

```

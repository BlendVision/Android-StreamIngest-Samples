# android-bv-streamingest

The streamingest SDK is a streaming solution based on the RTMP protocol.

## Requirements

- **IDE**: Android Studio 3.0 or later
- **minSdkVersion**: 24
- **targetSdkVersion**: 34
- **Kotlin Version**: 1.9.x or later

## Importing AAR into Project

There are multiple ways to integrate an AAR file into your Android project. Below are the methods,
including a simplified approach using `implementation fileTree`.

- `streamingest-core.aar`
- `streamingest-common.aar`
- `streamingest-encoder.aar`
- `streamingest-rtmp.aar`
- `streamingest-rtsp.aar`
- `streamingest-srt.aar`
- `streamingest.aar`

### Manual Import with `fileTree`

1. **Place AAR File in `libs` Directory**: Ensure the `.aar` (and/or `.jar`) file is located within
   the `libs` directory of your Android project.

2. **Update `build.gradle`**: In your app-level `build.gradle` file, add the following line in
   the `dependencies` block:

    ```groovy
    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
    ```

   This will include all `.jar` and `.aar` files that are in the `libs` directory into your project.

   > **Note**: After making changes, don't forget to sync your Gradle files to ensure that the
   project

## Usage

### 1. Setup

Add `configChanges` tag in your `AndroidManifest.xml` to prevent the activity from being recreated
during screen orientation changes.

```xml
<activity 
        android:name=".MainActivity" 
        android:configChanges="keyboardHidden|orientation|screenSize"
        android:exported="true">
   <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LAUNCHER" />
   </intent-filter>
</activity>
```

### 2. Integrate `StreamingestView` in Your Layout

This view provides a preview rendering from the camera.

```xml
<com.blendvision.stream.ingest.ui.view.StreamIngestView 
        android:id="@+id/stream_ingest_view"
        android:layout_width="match_parent" 
        android:layout_height="match_parent" />
```

### 3. Start streaming

Set up a stream state listener, and start publishing your stream.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
   super.onCreate(savedInstanceState)

   initListener()
   
   //StreamQuality.Low()、StreamQuality.Medium()、StreamQuality.High()
   streamIngestView.setStreamQuality(StreamQuality.High())

   streamIngestView.startStream("YOUR_RTMP_URL")

}
private fun initListener() {
   streamIngestView.streamStatus.flowWithLifecycle(lifecycle).onEach { state ->
      when (state) {
         State.INITIALIZED -> {
            //initialized.
         }

         State.CONNECT_STARTED -> {
            //connect stared.
         }

         State.CONNECT_SUCCESS -> {
            //connect success
         }

         State.DISCONNECT -> {
            //disconnect
         }
      }
   }.launchIn(lifecycleScope)

   streamIngestView.error.flowWithLifecycle(lifecycle).onEach { error ->
      //error.message
   }.launchIn(lifecycleScope)
}
```

Stops the ongoing streaming process.

```kotlin
streamIngestView.stopStream()
```

### 4. release stream

Please ensure to release the streamingest view to prevent memory leaks when the current page is
destroyed.

```kotlin
override fun onDestroy() {
   super.onDestroy()
   streamIngestView.release()
}
```

### 5. Beauty filter

Register beauty filters to optimize streaming.
```kotlin=

val skinSmoothFilter: BeautyFilter = SkinSmoothFilter(50)
streamIngestView.registerFilter(listOf(skinSmoothFilter))


sealed interface BeautyFilter {
    data class SkinSmoothFilter(
        val intensity: Int
    ) : BeautyFilter

    ...
}
```

Unregister the filter when you want to rollback to original streaming.
```kotlin
streamIngestView.unregisterFilter(listOf(skinSmoothFilter))
```


## Others StreamIngestView API

```kotlin

/**
 * Stops the ongoing streaming process.
 */
streamIngestView.stopStream()

/**
 * Sets the stream quality for the ongoing streaming process.
 * StreamQuality.Low()、StreamQuality.Medium()、StreamQuality.High()
 */
streamIngestView.setStreamQuality(StreamQuality.High())

/**
 * Switches the camera between front and back.
 */
streamIngestView.switchCamera()

/**
 * Initiates the streaming process with the provided RTMP URL.
 */
streamIngestView.startStream(rtmpUrl)

/**
 * Mutes or unmutes the video stream based on the given flag.
 */
streamIngestView.mutedVideo(isMute)

/**
 * Mutes or unmutes the audio stream based on the given flag.
 */
streamIngestView.mutedAudio(isMute)

/**
 * Starts the camera preview for the streaming process.
 */
streamIngestView.startCameraPreview()

/**
 * Stops the camera preview for the streaming process.
 */
streamIngestView.stopCameraPreview()

/**
 * Releases resources associated with the streaming process.
 */
streamIngestView.release()

```

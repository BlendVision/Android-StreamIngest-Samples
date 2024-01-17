# Android-Streamingest-SDK

The streamingest SDK is a streaming solution based on the RTMP protocol.

## Requirements

- **IDE**: Android Studio 3.0 or later
- **minSdkVersion**: 21
- **targetSdkVersion**: 34
- **Kotlin Version**: 1.9.x or later

## Importing AAR into Project

There are multiple ways to integrate an AAR file into your Android project. Below are the methods,
including a simplified approach using `implementation fileTree`.

- `streamingest-core.aar`
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

   > **Note**: After making changes, don't forget to sync your Gradle files to ensure that the project

## Usage

### 1. Add configChanges tag in your AndroidManifest.xml to prevent the activity from being recreated during screen orientation changes.
```xml
<activity
android:name=".MainActivity"
    android:configChanges="keyboardHidden|orientation|screenSize" // add configChanges tag
    android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />

            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
</activity>
```

### 2. Integrate StreamingestView in Your Layout

```xml
<com.blendvision.stream.ingest.ui.view.StreamIngestView
   android:id="@+id/stream_ingest_view"
   android:layout_width="match_parent"
   android:layout_height="match_parent"
   app:layout_constraintBottom_toBottomOf="parent"
   app:layout_constraintEnd_toEndOf="parent"
   app:layout_constraintStart_toStartOf="parent"
   app:layout_constraintTop_toTopOf="parent" />
```

### 3. Listen to the stream state and connect to the RTMP server.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initListener()

    streamIngestView.connect("YOUR_RTMP_URL")

}
private fun initListener() {
    streamIngestView.streamStatus.collect { state ->
        when (state) {
            State.CONNECT_SUCCESS -> streamIngestView.startPublish("YOUR_STREAM_NAME")
            State.PUBLISH_START -> {}// start publish
            else -> Unit
        }
    }
}
```


### 4. please ensure to release the streamingest view to prevent memory leaks When the current page is destroyed.
```kotlin
override fun onStop() {
    super.onStop()
    streamIngestView.stopPublish()
}
```
```kotlin
override fun onDestroy() {
    super.onDestroy()
    streamIngestView.dispose()
}
```

## Others StreamIngestView API
```kotlin
//Sets the quality of the streaming video.
//StreamQuality.Low()、StreamQuality.Medium()、StreamQuality.High()
streamIngestView.setStreamQuality(StreamQuality.Medium())

//Switches an using camera front or back.
streamIngestView.switchCamera()

//Mutes or unmutes the video stream.
//isMute true to mute, false to unmute.
streamIngestView.mutedVideo(isMute)

//Mutes or unmutes the audio stream.
//isMute true to mute, false to unmute.
streamIngestView.mutedAudio(isMute)

//Connects to the specified RTMP URL.
//rtmpUrl The RTMP URL to connect to.
streamIngestView.connect(rtmpUrl)

//Publish of the stream with the given stream name.
//streamName The name of the stream to be published.
streamIngestView.startPublish(streamName)

//Stops the ongoing stream publishing.
streamIngestView.stopPublish()

//Releases resources associated with the stream.
streamIngestView.dispose()

```
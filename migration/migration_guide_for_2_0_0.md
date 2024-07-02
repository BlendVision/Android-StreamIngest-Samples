# Migration Guide: Upgrading from Version 1.x.x to 2.0.0

Upgrading from version 1.x.x to 2.0.0 introduces several breaking changes. This guide provides detailed instructions to help you migrate your project to the latest version smoothly. The major breaking
changes include:

1. Maven implementation from GitHub.
2. StreamIngest object initialization to support license validation

## 1. Maven Implementation from GitHub

### Previous version (1.x.x)

In version 1.x.x, you might import the StreamIngest SDK as below which under project libs folder.

- libs folder include:
    - beauty-library-core.aar
    - beauty-library-face-tracking.aar
    - beauty-library-handler-core.aar
    - beauty-library-makeup.aar
    - beauty-library-product-handler.aar
    - streamingest-common.aar
    - streamingest-core.aar
    - streamingest-encoder.aar
    - streamingest-rtmp.aar
    - streamingest-rtsp.aar
    - streamingest-srt.aar
    - streamingest.aar

- Import StreamIngest SDK

```groovy=
implementation fileTree(dir: '../libs', include: ['*.jar', "*.aar"])
```

### New version (2.0.0)

Start from version 2.0.0. In order to let the dependency management streamlined, not only the project artifacts are now hosted on Github package repository, but also in libs folder.

- libs folder include only:
    - beauty-library-core.aar
    - beauty-library-face-tracking.aar
    - beauty-library-handler-core.aar
    - beauty-library-makeup.aar
    - beauty-library-product-handler.aar

- Pre-defined repo
  Username and Password can be found in [HERE](https://github.com/BlendVision/Android-StreamIngest-SDK/wiki/Android%E2%80%90StreamIngest%E2%80%90SDK-pull-credentials)

```groovy=
//-----add below------//
maven { url = uri("https://jitpack.io") }
maven {
    url = uri("https://maven.pkg.github.com/blendvision/Android-StreamIngest-SDK")
    credentials {
        username = //TODO
        password = //TODO
    }
}
```

- Import StreamIngest SDK

```groovy=
implementation fileTree(dir: '../libs', include: ['*.jar', "*.aar"])
implementation 'com.blendvision.stream.ingest:streamingest:2.0.0'
```

## 2. StreamIngest object initialization to support license validation

### Previous version (1.x.x)

Calling method directly from the StreamIngest View

```kotlin=
// Start streaming
val url: String = "rtmp://xxxx/${streamKey}"
binding.streamIngestView.startStream(url)
// Stop streaming
binding.streamIngestView.stopStream()
```

### New version (2.0.0)

- Craete the `StreamIngest` instance and set it to view

```kotlin=
val streamConfig = StreamConfig(licenseKey = licenseKey)
StreamIngest.Factory(context, streamConfig).create(object : ValidationListener {
    override fun onValidationSuccess(streamIngest: StreamIngest) {
        binding.streamIngestView.streamIngest = streamIngest
    }

    override fun onValidationFailed(exception: Exception) {
        // error by ${exception}, ex: license validate failed
    }
})
```

- Basic operation

```kotlin=
// start streaming
streamIngest.startStream(rtmpUrl, streamKey)

// stop streaming
streamIngest.stopStream()
```

## Summary

Upgrading from version 1.x.x to 2.0.0 involves updating your Maven dependencies, create new instance with license. Following this guide will help ensure a smooth transition to the latest version.
Always remember to thoroughly test your application after making these changes to verify that everything works as expected.

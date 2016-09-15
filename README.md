# WaveInApp [![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)](https://github.com/sindresorhus/awesome) <img src="https://www.cleveroad.com/public/comercial/label-android.svg" height="20"> <a href="https://www.cleveroad.com/?utm_source=github&utm_medium=label&utm_campaign=contacts"><img src="https://www.cleveroad.com/public/comercial/label-cleveroad.svg" height="20"></a>
![Header image](/images/header.jpg)

## Welcome to WaveInApp - Audio Visualization View with wave effect.

Our library can take audio from any source (audio players, streams, voice input) and animate it with high frame rate. Cool animation, designed specially for the library, responds to sound vibrations. The animation becomes intense when music plays, and once it is paused or stopped – waves calm down.

The library is a part of implementation of <a href="https://dribbble.com/shots/2369760-Player-Concept">music player</a>.
<br/>

![Demo image](/images/demo_.gif)


Great visualization can spruce up any app, especially audio player. We suggest you smart and good-looking Audio Visualization View for your Android app. You can read about all the advantages this library has and find out how to implement it into your app in our blog post: <b><a href="https://www.cleveroad.com/blog/case-study-audio-visualization-view-for-android-by-cleveroad">Case Study: Audio Visualization View For Android by Cleveroad</a></b>
<br/><br/>

[![Article image](/images/article.jpg)](https://www.cleveroad.com/blog/case-study-audio-visualization-view-for-android-by-cleveroad)
<br/><br/>
[![Awesome](/images/logo-footer.png)](https://www.cleveroad.com/?utm_source=github&utm_medium=label&utm_campaign=contacts)
<br/>

## Setup and usage

To include this library to your project add dependency in **build.gradle** file:

```groovy
    dependencies {
        compile 'com.cleveroad:audiovisualization:0.9.4'
    }
```

Audio visualization view uses OpenGL ES 2.0 for drawing waves. So you need to include this line in your manifest:

```XML        
    <uses-feature android:glEsVersion="0x00020000" android:required="true" />
```

##### Using VisualizerDbmHandler

All functionality of this handler built upon [Visualizer] object, so you also need to include this permissions in your manifest:

```XML
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
```

##### Using SpeechRecognizerDbmHandler

All functionality of this handler built upon [SpeechRecognizer] object, so you also need to include this permissions in your manifest:

```XML
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

You must be very careful with new [Android M permissions] flow. Make sure you have all necessary permissions before using **GLAudioVisualizationView**.

There are two ways to include **GLAudioVisualizationView** in your layout: directly in XML layout file or using builder in Java code.

Via XML:

```XML
    <com.cleveroad.audiovisualization.GLAudioVisualizationView
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/visualizer_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:av_bubblesSize="@dimen/bubble_size"
        app:av_bubblesRandomizeSizes="true"
        app:av_wavesHeight="@dimen/wave_height"
        app:av_wavesFooterHeight="@dimen/footer_height"
        app:av_wavesCount="7"
        app:av_layersCount="4"
        app:av_backgroundColor="@color/av_color_bg"
        app:av_bubblesPerLayer="16"
        />
```

Via Java code:

```JAVA
    new GLAudioVisualizationView.Builder(getContext())
        .setBubblesSize(R.dimen.bubble_size)
        .setBubblesRandomizeSize(true)
        .setWavesHeight(R.dimen.wave_height)
        .setWavesFooterHeight(R.dimen.footer_height)
        .setWavesCount(7)
        .setLayersCount(4)
        .setBackgroundColorRes(R.color.av_color_bg)
        .setLayerColors(R.array.av_colors)
        .setBubblesPerLayer(16)
        .build();
```

**GLAudioVisualizationView** implements **AudioVisualization** interface. If you don't need all [GLSurfaceView]'s public methods, you can simply cast your view to **AudioVisualization** interface and use it.

```JAVA
    private AudioVisualization audioVisualization;
    
    ...
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // you can extract AudioVisualization interface for simplifying things
        audioVisualization = (AudioVisualization) glAudioVisualizationView;
    }

    ...
```

To connect audio visualization view to audio output you can use **linkTo(DbmHandler)** method. See **DbmHandler.Factory** class for the list of available handler implementations.

```JAVA
    // set speech recognizer handler
    SpeechRecognizerDbmHandler speechRecHandler = DbmHandler.Factory.newSpeechRecognizerHandler(context);
    speechRecHandler.innerRecognitionListener(...);
    audioVisualization.linkTo(speechRecHandler);
    
    // set audio visualization handler. This will REPLACE previously set speech recognizer handler
    VisualizerDbmHandler vizualizerHandler = DbmHandler.Factory.newVisualizerHandler(getContext(), 0);
    audioVisualization.linkTo(vizualizerHandler);
```

You must always call **onPause** method to pause visualization and stop wasting CPU resources for computations in vain. As soon as your view appears in sight of user, call **onResume**. 

```JAVA
    @Override
    public void onResume() {
        super.onResume();
        audioVisualization.onResume();
    }
    
    @Override
    public void onPause() {
        audioVisualization.onPause();
        super.onPause();
    }
```

When user leaves screen with audio visualization view, don't forget to free resources and call **release()** method.

```JAVA
    @Override
    public void onDestroyView() {
        audioVisualization.release();
        super.onDestroyView();
    }
```

## Live wallpapers
You can use our Audio Visualization View as a live wallpaper. Just create your own [WallpaperService]. Method `onCreateEngine()` should return your own [Engine]'s implementation, in which you must override the following methods:
* `void onCreate(SurfaceHolder surfaceHolder)` – here create instances of `DbmHandler`, `GLAudioVisualizationView.Builder` (see example below) and `GLAudioVisualizationView.AudioVisualizationRenderer` in which you must set `Engine`'s surface holder and two previous instances via `constructor(GLAudioVisualizationView.Builder)` and `handler()` methods;
* `void onVisibilityChanged(final boolean visible)` – here you must call `onResume()` methods for audioVisualizationView and dbmHandler instances if `visible` parameter is `true`, otherwise – call `onPause()`;
* `void onDestroy()` – just call `release()` for dbmHandler and `onDestroy()` for audioVisualizationView instances
Check JavaDoc of this methods for more info.

```JAVA
    public class AudioVisualizationWallpaperService extends WallpaperService {
        @Override
        public Engine onCreateEngine() {
            return new WallpaperEngine();
        }
        
        private class WallpaperEngine extends Engine {
        
        private WallpaperGLSurfaceView audioVisualizationView;
        private DbmHandler dbmHandler;
        private GLAudioVisualizationView.AudioVisualizationRenderer renderer;
        ...
            @Override
            public void onCreate(SurfaceHolder surfaceHolder) {
                AudioVisualizationWallpaperService context = AudioVisualizationWallpaperService.this;
                audioVisualizationView = new WallpaperGLSurfaceView(context);
                dbmHandler = DbmHandler.Factory.newVisualizerHandler(context, 0);
                ...
                GLAudioVisualizationView.Builder builder = new GLAudioVisualizationView.Builder(context)
                        //... set your settings here (see builder example below);
                renderer = new GLAudioVisualizationView.RendererBuilder(builder)
                        .glSurfaceView(audioVisualizationView)
                        .handler(dbmHandler)
                        .build();
                audioVisualizationView.setEGLContextClientVersion(2);
                audioVisualizationView.setRenderer(renderer);
            }
            @Override
            public void onVisibilityChanged(final boolean visible) {
                //Please follow the next order of methods call!
                if (visible) {
                    audioVisualizationView.onResume();
                    dbmHandler.onResume();
                } else {
                    dbmHandler.onPause();
                    audioVisualizationView.onPause();
                }
            }
            @Override
            public void onDestroy() {
                dbmHandler.release();
                audioVisualizationView.onDestroy();
            }
         
```
See uploaded [AudioVisualizationWallpaperService example].  
<br/>

## Implementing your own DbmHandler
To implement you own data conversion handler, just extend your class from DbmHandler class and implement **onDataReceivedImpl(T object, int layersCount, float[] outDbmValues, float[] outAmpValues)** method where:
* `object` - your custom data type
* `layersCount` - count of layers you passed in **Builder**.
* `outDbmValues` - array with size equals to `layersCount`. You should fill it with **normalized** dBm values for layer in range [0..1].
* `outAmpValues` - array with size equals to `layersCount`. You should fill it with amplitude values for layer.
Check JavaDoc of this method for more info.

Then call **onDataReceived(T object)** method to visualize your data.

Your handler also will receive **onResume()**, **onPause()** and **release()** events from audio visualization view.
<br />

## Migrations
See all [migration manuals].

## Changelog
See [changelog history].
<br />

## Troubleshooting
#### Visualization
If you have some issues with visualization (especially on Samsung Galaxy S or HTC devices) make sure you read [this Github issue].
#### Live wallpapers
* If you have some issues with WaveInApp live wallpapers on Android 6.0 (and later) make sure that all next permissions are granted: `android.permission.RECORD_AUDIO`, `android.permission.MODIFY_AUDIO_SETTINGS`.
* If you run a wallpaper selection screen when the permissions was not granted, the wallpaper will not be appear. You must open settings screen and allow all permissions. If the wallpaper is not yet appeared, just restart the wallpaper selection screen.
<br />

## Support
If you have any other questions regarding the use of this library, please contact us for support at info@cleveroad.com (email subject: "Android visualization view. Support request.") 
<br />

## License
* * *
    The MIT License (MIT)
    
    Copyright (c) 2016 Cleveroad Inc.
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

[migration manuals]: /MIGRATION.md
[changelog history]: /CHANGELOG.md
[AudioVisualizationWallpaperService example]: wallpaper/src/main/java/com/cleveroad/wallpaper/AudioVisualizationWallpaperService.java
[WallpaperService]: https://developer.android.com/reference/android/service/wallpaper/WallpaperService.html
[this Github issue]: https://github.com/felixpalmer/android-visualizer/issues/5#issuecomment-25900391
[Engine]: https://developer.android.com/reference/android/service/wallpaper/WallpaperService.Engine.html
[Visualizer]: http://developer.android.com/intl/ru/reference/android/media/audiofx/Visualizer.html
[SpeechRecognizer]: http://developer.android.com/intl/ru/reference/android/speech/SpeechRecognizer.html
[Android M permissions]: http://developer.android.com/intl/ru/training/permissions/requesting.html
[GLSurfaceView]: http://developer.android.com/intl/ru/reference/android/opengl/GLSurfaceView.html

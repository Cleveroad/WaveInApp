# Audio Visualization View #

Implementation of [music player concept](https://dribbble.com/shots/2369760-Player-Concept). Welcome Live Equalizer with wave effect. As animation shows, when music plays the waves are active, and once it is paused or stopped – waves calm down.

![Demo image](/images/demo.gif)

To include this library to your project add dependency in **build.gradle** file:

```groovy
    dependencies {
        compile 'com.cleveroad:audiovisualization:0.9.0'
    }
```

Setup process can be splitted into few steps.

<br />
#### Preparing AndroidManifest.xml ####
* * *
Audio visualization view uses OpenGL ES 2.0 for drawing waves. So you need to include this line in your manifest:

```XML        
    <uses-feature android:glEsVersion="0x00020000" android:required="true" />
```

All functionality built upon [Visualizer](http://developer.android.com/intl/ru/reference/android/media/audiofx/Visualizer.html) object, so you also need to include this permissions in your manifest:

```XML
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
```

You must be very careful with new [Android M permissions](http://developer.android.com/intl/ru/training/permissions/requesting.html) flow. Make sure you have all necessary permissions before using **GLAudioVisualizationView**.

<br />
#### Including **GLAudioVisualizationView** into layout ####
* * *

There are two ways to include **GLAudioVisualizationView** in your layout: directly in XML layout file or using builder in Java code.

Via XML:

```XML
    <com.cleveroad.audiovisualization.GLAudioVisualizationView
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/visualizer_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:bubble_size="@dimen/bubble_size"
        app:randomize_bubble_size="true"
        app:wave_height="@dimen/wave_height"
        app:footer_height="@dimen/footer_height"
        app:waves_count="7"
        app:layers_count="4"
        app:background_color="@color/color_bg"
        />
```

Via Java code:

```JAVA
    new GLAudioVisualizationView.Builder(getContext())
        .setBubbleSize(R.dimen.bubble_size)
        .setRandomizeBubbleSize(true)
        .setWaveHeight(R.dimen.wave_height)
        .setFooterHeight(R.dimen.footer_height)
        .setWavesCount(7)
        .setLayersCount(4)
        .setBackgroundColorRes(R.color.color_bg)
        .setLayerColors(R.array.colors)
        .build();
```
    
<br />
#### Usage of **GLAudioVisualizationView** ####
* * *

**GLAudioVisualizationView** implements **AudioVisualization** interface. If you don't need all [GLSurfaceView](http://developer.android.com/intl/ru/reference/android/opengl/GLSurfaceView.html)'s public methods, you can simply cast your view to **AudioVisualization** interface and use it.

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

To connect audio visualization view to audio output you can use **linkTo(int)** or **linkTo(MediaPlayer)** methods.

```JAVA
    // connecting to device's output mix
    audioVisualization.linkTo(0);
    ...
    MediaPlayer mp = MediaPlayer.create(...);
    audioVisualization.linkTo(mp);
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

<br />
#### Support ####
* * *
If you have some issues with visualization (especially on Samsung Galaxy S or HTC devices) make sure you read [this Github issue](https://github.com/felixpalmer/android-visualizer/issues/5#issuecomment-25900391). 

If you have any other questions regarding the use of this library, please contact us for support at info@cleveroad.com (email subject: "Android visualization view. Support request.") 
or 

Use our contacts: 

* [Official site](https://www.cleveroad.com/?utm_source=github&utm_medium=link&utm_campaign=contacts)
* [Facebook account](https://www.facebook.com/cleveroadinc)
* [Twitter account](https://twitter.com/CleveroadInc)
* [Google+ account](https://plus.google.com/+CleveroadInc/)

<br />
#### License ####
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

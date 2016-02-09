# Audio Visualization View #
* * *

OpenGL ES 2.0 implementation of [music player concept](https://dribbble.com/shots/2369760-Player-Concept).

![Alt text](http://www.addictedtoibiza.com/wp-content/uploads/2012/12/example.png)

### Usage ###
* * *
* Add this lines to your **AndroidManifest.xml** file:
    
```XML
	<uses-permission android:name="android.permission.RECORD_AUDIO"/>
	<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
	
	<uses-feature android:glEsVersion="0x00020000" android:required="true" />
```

* Add **GLAudioVisualizationView** in your layout file:

```XML
<com.cleveroad.audiovisualization.GLAudioVisualizationView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/visualizer_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:bubble_size="25dp"
    app:randomize_bubble_size="true"
    app:wave_height="60dp"
    app:footer_height="170dp"
    app:waves_count="7"
    app:layers_count="4"
    app:background_color="@color/color_bg"
    />
```

* In your activity or fragment find this view and use it.

```JAVA
private AudioVisualization audioVisualization;
...
@Override
public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
	super.onViewCreated(view, savedInstanceState);
        // you can extract AudioVisualization interface for simplifying things
	audioVisualization = (AudioVisualization) view.findViewById(R.id.visualizer_view);
        // link to system output. You can use linkTo(MediaPlayer) instead or provide any other unique audio session id (for example, from AudioTrack)
	audioVisualization.linkTo(0);
}

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

@Override
public void onDestroyView() {
	audioVisualization.release();
	super.onDestroyView();
}
```

### Support ###
* * *
If you have any questions regarding the use of this tutorial, please contact us for support at info@cleveroad.com (email subject: «Sliding iOS app tutorial. Support request.») 
or 

Use our contacts: 

* [Cleveroad.com](https://www.cleveroad.com/?utm_source=github&utm_medium=link&utm_campaign=contacts)
* [Facebook account](https://www.facebook.com/cleveroadinc)
* [Twitter account](https://twitter.com/CleveroadInc)
* [Google+ account](https://plus.google.com/+CleveroadInc/)

### License ###
* * *
    The MIT License (MIT)

    Copyright (c) 2015-2016 Cleveroad

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
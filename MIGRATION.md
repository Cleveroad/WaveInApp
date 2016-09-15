# WaveInApp [![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)](https://github.com/sindresorhus/awesome) <img src="https://www.cleveroad.com/public/comercial/label-android.svg" height="20"> <a href="https://www.cleveroad.com/?utm_source=github&utm_medium=label&utm_campaign=contacts"><img src="https://www.cleveroad.com/public/comercial/label-cleveroad.svg" height="20"></a>
![Header image](/images/header.jpg)

## Migration from v.0.9.2 to v.0.9.3
All attributes and appropriate methods in builder were renamed.

| Old name | New name |
| --- | --- |
| av_waves_count | av_wavesCount |
| av_waves_colors | av_wavesColors |
| av_wave_height | av_wavesHeight |
| av_footer_height | av_wavesFooterHeight |
| av_bubble_size | av_bubblesSize |
| av_randomize_bubble_size | av_bubblesRandomizeSizes |
| av_layers_count | av_layersCount |
| av_background_color | av_backgroundColor |

## Migration from v.0.9.1 to v.0.9.2
* **DbmHandler.Factory.newVisualizerHandler(int)** method signature changed to **DbmHandler.Factory.newVisualizerHandler(Context, int)**.

## Migration from v.0.9.0 to v.0.9.1
* All library resources and attributes were renamed, prefix `av_` was added. If you're adding Audio Visualization view through XML, make sure you'd properly renamed all attributes. See updated example above. 
* All library resources were marked as private. If you're pointing to any library resource (color, dimen, etc), Android Studio will warn you.
* All calculations of dBm values were moved to separate classes. Now you should use **DbmHandler.Factory** class to create new handlers and link it to audio visualization view using **linkTo(DbmHandler)** method. You can provide your implementation of DbmHandler as well.
* **setInnerOnPreparedListener()** and **setInnerOnCompletionListener()** methods moved to new **VisualizerDbmHandler** class.
<br />

## Support
If you have any other questions regarding the use of this library, please contact us for support at info@cleveroad.com (email subject: "Android visualization view. Support request.") 

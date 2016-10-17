package com.cleveroad.audiovisualization;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

/**
 * Audio visualization view implementation for OpenGL.
 */
public class GLAudioVisualizationView extends GLSurfaceView implements AudioVisualization, InnerAudioVisualization {

    private static final int EGL_VERSION = 2;

    private final Configuration mConfiguration;
    private final GLRenderer mRenderer;
    @Nullable
    private CalmDownListener mCalmDownListener;
    private DbmHandler<?> mDbmHandler;

    private GLAudioVisualizationView(@NonNull Builder builder) {
        super(builder.mContext);
        mConfiguration = new Configuration(builder);
        mRenderer = new GLRenderer(getContext(), mConfiguration);
        init();
    }

    public GLAudioVisualizationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mConfiguration = new Configuration(context, attrs, isInEditMode());
        mRenderer = new GLRenderer(getContext(), mConfiguration);
        init();
    }

    @Override
    public void calmDownListener(@Nullable CalmDownListener calmDownListener) {
        mCalmDownListener = calmDownListener;
    }

    @Override
    public <T> void linkTo(@NonNull DbmHandler<T> dbmHandler) {
        if (mDbmHandler != null) {
            mDbmHandler.release();
        }
        mDbmHandler = dbmHandler;
        mDbmHandler.setUp(this, mConfiguration.mLayersCount);
    }

    @Override
    public void release() {
        if (mDbmHandler != null) {
            mDbmHandler.release();
            mDbmHandler = null;
        }
    }

    @Override
    public void onPause() {
        if (mDbmHandler != null) {
            mDbmHandler.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDbmHandler != null) {
            mDbmHandler.onResume();
        }
    }

    private void init() {
        setEGLContextClientVersion(EGL_VERSION);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderer(mRenderer);
        mRenderer.calmDownListener(new CalmDownListener() {
            @Override
            public void onCalmedDown() {
                stopRendering();
                if (mCalmDownListener != null) {
                    mCalmDownListener.onCalmedDown();
                }
            }
        });
    }

    /**
     * Audio Visualization renderer interface that allows to change waves' colors at runtime.
     */
    public interface AudioVisualizationRenderer extends Renderer {

        /**
         * Update colors configuration.
         *
         * @param builder instance of color builder.
         */
        void updateConfiguration(@NonNull ColorsBuilder builder);
    }

    /**
     * Configuration holder class.
     */
    static class Configuration {

        float[] mBackgroundColor;
        float mBubbleSize;
        int mBubblesPerLayer;
        float mFooterHeight;
        float[][] mLayerColors;
        int mLayersCount;
        boolean mRandomizeBubbleSize;
        float mWaveHeight;
        int mWavesCount;

        Configuration(Context context, AttributeSet attrs, boolean isInEditMode) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GLAudioVisualizationView);
            int[] colors;
            int bgColor;
            try {
                mLayersCount = array.getInt(R.styleable.GLAudioVisualizationView_av_layersCount, Constants.DEFAULT_LAYERS_COUNT);
                mLayersCount = Utils.between(mLayersCount, Constants.MIN_LAYERS_COUNT, Constants.MAX_LAYERS_COUNT);
                mWavesCount = array.getInt(R.styleable.GLAudioVisualizationView_av_wavesCount, Constants.DEFAULT_WAVES_COUNT);
                mWavesCount = Utils.between(mWavesCount, Constants.MIN_WAVES_COUNT, Constants.MAX_WAVES_COUNT);
                mWaveHeight = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_av_wavesHeight, (int) Constants.DEFAULT_WAVE_HEIGHT);
                mWaveHeight = Utils.between(mWaveHeight, Constants.MIN_WAVE_HEIGHT, Constants.MAX_WAVE_HEIGHT);
                mBubbleSize = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_av_bubblesSize, Constants.DEFAULT_BUBBLE_SIZE);
                mBubbleSize = Utils.between(mBubbleSize, Constants.MIN_BUBBLE_SIZE, Constants.MAX_BUBBLE_SIZE);
                mRandomizeBubbleSize = array.getBoolean(R.styleable.GLAudioVisualizationView_av_bubblesRandomizeSizes, false);
                mFooterHeight = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_av_wavesFooterHeight, (int) Constants.DEFAULT_FOOTER_HEIGHT);
                mFooterHeight = Utils.between(mFooterHeight, Constants.MIN_FOOTER_HEIGHT, Constants.MAX_FOOTER_HEIGHT);
                mBubblesPerLayer = array.getInt(R.styleable.GLAudioVisualizationView_av_bubblesPerLayer, Constants.DEFAULT_BUBBLES_PER_LAYER);
                mBubblesPerLayer = Utils.between(mBubblesPerLayer, Constants.DEFAULT_BUBBLES_PER_LAYER_MIN, Constants.DEFAULT_BUBBLES_PER_LAYER_MAX);
                bgColor = array.getColor(R.styleable.GLAudioVisualizationView_av_backgroundColor, Color.TRANSPARENT);
                if (bgColor == Color.TRANSPARENT) {
                    bgColor = ContextCompat.getColor(context, R.color.av_color_bg);
                }
                int arrayId = array.getResourceId(R.styleable.GLAudioVisualizationView_av_wavesColors, R.array.av_colors);
                if (isInEditMode) {
                    colors = new int[mLayersCount];
                } else {
                    TypedArray colorsArray = array.getResources().obtainTypedArray(arrayId);
                    colors = new int[colorsArray.length()];
                    for (int i = 0; i < colorsArray.length(); i++) {
                        colors[i] = colorsArray.getColor(i, Color.TRANSPARENT);
                    }
                    colorsArray.recycle();
                }
            } finally {
                array.recycle();
            }
            if (colors.length < mLayersCount) {
                throw new IllegalArgumentException("You specified more layers than colors.");
            }

            mLayerColors = new float[colors.length][];
            for (int i = 0; i < colors.length; i++) {
                mLayerColors[i] = Utils.convertColor(colors[i]);
            }
            mBackgroundColor = Utils.convertColor(bgColor);
            mBubbleSize /= context.getResources().getDisplayMetrics().widthPixels;
        }

        private Configuration(@NonNull Builder builder) {
            mWaveHeight = builder.mWaveHeight;
            mWaveHeight = Utils.between(mWaveHeight, Constants.MIN_WAVE_HEIGHT, Constants.MAX_WAVE_HEIGHT);
            mWavesCount = builder.mWavesCount;
            mWavesCount = Utils.between(mWavesCount, Constants.MIN_WAVES_COUNT, Constants.MAX_WAVES_COUNT);
            mLayerColors = builder.getLayerColors();
            mBubbleSize = builder.mBubbleSize;
            mBubbleSize = Utils.between(mBubbleSize, Constants.MIN_BUBBLE_SIZE, Constants.MAX_BUBBLE_SIZE);
            mBubbleSize = mBubbleSize / builder.mContext.getResources().getDisplayMetrics().widthPixels;
            mFooterHeight = builder.mFooterHeight;
            mFooterHeight = Utils.between(mFooterHeight, Constants.MIN_FOOTER_HEIGHT, Constants.MAX_FOOTER_HEIGHT);
            mRandomizeBubbleSize = builder.mRandomizeBubbleSize;
            mBackgroundColor = builder.getBackgroundColor();
            mLayersCount = builder.mLayersCount;
            mBubblesPerLayer = builder.mBubblesPerLayer;
            Utils.between(mBubblesPerLayer, Constants.DEFAULT_BUBBLES_PER_LAYER_MIN, Constants.DEFAULT_BUBBLES_PER_LAYER_MAX);
            mLayersCount = Utils.between(mLayersCount, Constants.MIN_LAYERS_COUNT, Constants.MAX_LAYERS_COUNT);
            if (mLayerColors.length < mLayersCount) {
                throw new IllegalArgumentException("You specified more layers than colors.");
            }
        }
    }

    public static class ColorsBuilder<T extends ColorsBuilder> {
        @NonNull
        private final Context mContext;

        @NonNull
        private float[] mBackgroundColor = Utils.convertColor(Color.TRANSPARENT);

        private float[][] mLayerColors;

        public ColorsBuilder(@NonNull Context context) {
            mContext = context;
        }

        /**
         * Set background color
         *
         * @param backgroundColor background color
         */
        public T setBackgroundColor(@ColorInt int backgroundColor) {
            mBackgroundColor = Utils.convertColor(backgroundColor);
            return getThis();
        }

        /**
         * Set background color from color resource
         *
         * @param backgroundColor color resource
         */
        public T setBackgroundColorRes(@ColorRes int backgroundColor) {
            return setBackgroundColor(ContextCompat.getColor(mContext, backgroundColor));
        }

        /**
         * Set layer colors.
         *
         * @param colors array of colors
         */
        public T setLayerColors(@NonNull int[] colors) {
            mLayerColors = new float[colors.length][];
            for (int i = 0; i < colors.length; i++) {
                mLayerColors[i] = Utils.convertColor(colors[i]);
            }
            return getThis();
        }

        /**
         * Set layer colors from array resource
         *
         * @param arrayId array resource
         */
        public T setLayerColors(@ArrayRes int arrayId) {
            TypedArray colorsArray = mContext.getResources().obtainTypedArray(arrayId);
            int[] colors = new int[colorsArray.length()];
            for (int i = 0; i < colorsArray.length(); i++) {
                colors[i] = colorsArray.getColor(i, Color.TRANSPARENT);
            }
            colorsArray.recycle();
            return setLayerColors(colors);
        }

        protected T getThis() {
            //noinspection unchecked
            return (T) this;
        }

        @NonNull
        float[] getBackgroundColor() {
            return mBackgroundColor;
        }

        float[][] getLayerColors() {
            return mLayerColors;
        }
    }

    public static class Builder extends ColorsBuilder<Builder> {

        private float mBubbleSize;
        private int mBubblesPerLayer;
        private Context mContext;
        private float mFooterHeight;
        private int mLayersCount;
        private boolean mRandomizeBubbleSize;
        private float mWaveHeight;
        private int mWavesCount;

        public Builder(@NonNull Context context) {
            super(context);
            mContext = context;
        }

        public GLAudioVisualizationView build() {
            if (getLayerColors() == null) {
                throw new IllegalArgumentException("You must specify layer colors.");
            }
            return new GLAudioVisualizationView(this);
        }

        /**
         * Set number of bubbles per layer.
         *
         * @param bubblesPerLayer number of bubbles per layer
         */
        public Builder setBubblesPerLayer(int bubblesPerLayer) {
            mBubblesPerLayer = bubblesPerLayer;
            return this;
        }

        /**
         * Set flag indicates that size of bubbles should be randomized
         *
         * @param randomizeBubbleSize true if size of bubbles should be randomized, false if size of bubbles must be the same
         */
        public Builder setBubblesRandomizeSize(boolean randomizeBubbleSize) {
            mRandomizeBubbleSize = randomizeBubbleSize;
            return this;
        }

        /**
         * Set bubbles size in pixels
         *
         * @param bubbleSize bubbles size in pixels
         */
        public Builder setBubblesSize(float bubbleSize) {
            mBubbleSize = bubbleSize;
            return this;
        }

        /**
         * Set bubble size from dimension resource
         *
         * @param bubbleSize dimension resource
         */
        public Builder setBubblesSize(@DimenRes int bubbleSize) {
            return setBubblesSize((float) mContext.getResources().getDimensionPixelSize(bubbleSize));
        }

        /**
         * Set layers count
         *
         * @param layersCount layers count
         */
        public Builder setLayersCount(int layersCount) {
            mLayersCount = layersCount;
            return this;
        }

        /**
         * Set waves count
         *
         * @param wavesCount waves count
         */
        public Builder setWavesCount(int wavesCount) {
            mWavesCount = wavesCount;
            return this;
        }

        /**
         * Set footer height in pixels
         *
         * @param footerHeight footer height in pixels
         */
        public Builder setWavesFooterHeight(float footerHeight) {
            mFooterHeight = footerHeight;
            return this;
        }

        /**
         * Set footer height from dimension resource
         *
         * @param footerHeight dimension resource
         */
        public Builder setWavesFooterHeight(@DimenRes int footerHeight) {
            return setWavesFooterHeight((float) mContext.getResources().getDimensionPixelSize(footerHeight));
        }

        /**
         * Set wave height in pixels
         *
         * @param waveHeight wave height in pixels
         */
        public Builder setWavesHeight(float waveHeight) {
            mWaveHeight = waveHeight;
            return this;
        }

        /**
         * Set wave height from dimension resource
         *
         * @param waveHeight dimension resource
         */
        public Builder setWavesHeight(@DimenRes int waveHeight) {
            return setWavesHeight((float) mContext.getResources().getDimensionPixelSize(waveHeight));
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

    /**
     * Renderer builder.
     */
    public static class RendererBuilder {

        private final Builder mBuilder;
        private GLSurfaceView mGlSurfaceView;
        private DbmHandler mHandler;

        /**
         * Create new renderer using existing Audio Visualization builder.
         *
         * @param builder instance of Audio Visualization builder
         */
        public RendererBuilder(@NonNull Builder builder) {
            mBuilder = builder;
        }

        /**
         * Create new Audio Visualization Renderer.
         *
         * @return new Audio Visualization Renderer
         */
        public AudioVisualizationRenderer build() {
            final GLRenderer renderer = new GLRenderer(mBuilder.mContext, new Configuration(mBuilder));
            final InnerAudioVisualization audioVisualization = new InnerAudioVisualization() {
                @Override
                public void startRendering() {
                    if (mGlSurfaceView.getRenderMode() != RENDERMODE_CONTINUOUSLY) {
                        mGlSurfaceView.setRenderMode(RENDERMODE_CONTINUOUSLY);
                    }
                }

                @Override
                public void stopRendering() {
                    if (mGlSurfaceView.getRenderMode() != RENDERMODE_WHEN_DIRTY) {
                        mGlSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
                    }
                }

                @Override
                public void calmDownListener(@Nullable CalmDownListener calmDownListener) {

                }

                @Override
                public void onDataReceived(float[] dBmArray, float[] ampsArray) {
                    renderer.onDataReceived(dBmArray, ampsArray);
                }
            };
            renderer.calmDownListener(new CalmDownListener() {
                @Override
                public void onCalmedDown() {
                    audioVisualization.stopRendering();
                }
            });
            mHandler.setUp(audioVisualization, mBuilder.mLayersCount);
            return renderer;
        }

        /**
         * Set OpenGL surface view.
         *
         * @param glSurfaceView instance of OpenGL surface view
         */
        public RendererBuilder glSurfaceView(@NonNull GLSurfaceView glSurfaceView) {
            mGlSurfaceView = glSurfaceView;
            return this;
        }

        /**
         * Set dBm handler.
         *
         * @param handler instance of dBm handler
         */
        public RendererBuilder handler(DbmHandler handler) {
            mHandler = handler;
            return this;
        }
    }

    @Override
    public void startRendering() {
        if (getRenderMode() != RENDERMODE_CONTINUOUSLY) {
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }
    }


    @Override
    public void stopRendering() {
        if (getRenderMode() != RENDERMODE_WHEN_DIRTY) {
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }
    }


    @Override
    public void onDataReceived(float[] dBmArray, float[] ampsArray) {
        mRenderer.onDataReceived(dBmArray, ampsArray);
    }
}

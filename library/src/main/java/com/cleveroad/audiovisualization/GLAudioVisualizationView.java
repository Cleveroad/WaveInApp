package com.cleveroad.audiovisualization;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Audio visualization view implementation for OpenGL.
 */
public class GLAudioVisualizationView extends GLSurfaceView implements AudioVisualization, InnerAudioVisualization {

	private static final int EGL_VERSION = 2;
	private final GLRenderer renderer;
    private DbmHandler<?> dbmHandler;
	private final Configuration configuration;
    private CalmDownListener innerCalmDownListener;

	private GLAudioVisualizationView(@NonNull Builder builder) {
		super(builder.context);
		configuration = new Configuration(builder);
		renderer = new GLRenderer(getContext(), configuration);
		init();
	}

	public GLAudioVisualizationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		configuration = new Configuration(context, attrs, isInEditMode());
		renderer = new GLRenderer(getContext(), configuration);
		init();
	}

	private void init() {
		setEGLContextClientVersion(EGL_VERSION);
		setRenderer(renderer);
        renderer.calmDownListener(new CalmDownListener() {
            @Override
            public void onCalmedDown() {
                stopRendering();
                if (innerCalmDownListener != null) {
                    innerCalmDownListener.onCalmedDown();
                }
            }
        });
	}

	@Override
	public void onResume() {
		super.onResume();
        if (dbmHandler != null) {
            dbmHandler.onResume();
        }
	}

	@Override
	public void onPause() {
        if (dbmHandler != null) {
            dbmHandler.onPause();
        }
		super.onPause();
	}

    @Override
    public <T> void linkTo(@NonNull DbmHandler<T> dbmHandler) {
        if (this.dbmHandler != null) {
            this.dbmHandler.release();
        }
        this.dbmHandler = dbmHandler;
        this.dbmHandler.setUp(this, configuration.layersCount);
    }

	@Override
	public void release() {
		if (dbmHandler != null) {
			dbmHandler.release();
            dbmHandler = null;
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
    public void calmDownListener(@Nullable CalmDownListener calmDownListener) {
        innerCalmDownListener = calmDownListener;
    }

    @Override
    public void onDataReceived(float[] dBmArray, float[] ampsArray) {
        renderer.onDataReceived(dBmArray, ampsArray);
    }

    /**
	 * Configuration holder class.
	 */
	static class Configuration {

		int wavesCount;
		int layersCount;
		float bubbleSize;
		float waveHeight;
		float footerHeight;
		boolean randomizeBubbleSize;
		float[] backgroundColor;
		float[][] layerColors;

		public Configuration(Context context, AttributeSet attrs, boolean isInEditMode) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GLAudioVisualizationView);
			int[] colors;
			int bgColor;
			try {
				layersCount = array.getInt(R.styleable.GLAudioVisualizationView_av_layers_count, Constants.DEFAULT_LAYERS_COUNT);
				layersCount = Utils.between(layersCount, Constants.MIN_LAYERS_COUNT, Constants.MAX_LAYERS_COUNT);
				wavesCount = array.getInt(R.styleable.GLAudioVisualizationView_av_waves_count, Constants.DEFAULT_WAVES_COUNT);
				wavesCount = Utils.between(wavesCount, Constants.MIN_WAVES_COUNT, Constants.MAX_WAVES_COUNT);
				waveHeight = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_av_wave_height, (int) Constants.DEFAULT_WAVE_HEIGHT);
				waveHeight = Utils.between(waveHeight, Constants.MIN_WAVE_HEIGHT, Constants.MAX_WAVE_HEIGHT);
				bubbleSize = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_av_bubble_size, Constants.DEFAULT_BUBBLE_SIZE);
				bubbleSize = Utils.between(bubbleSize, Constants.MIN_BUBBLE_SIZE, Constants.MAX_BUBBLE_SIZE);
				randomizeBubbleSize = array.getBoolean(R.styleable.GLAudioVisualizationView_av_randomize_bubble_size, false);
				footerHeight = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_av_footer_height, (int) Constants.DEFAULT_FOOTER_HEIGHT);
				footerHeight = Utils.between(footerHeight, Constants.MIN_FOOTER_HEIGHT, Constants.MAX_FOOTER_HEIGHT);
				bgColor = array.getColor(R.styleable.GLAudioVisualizationView_av_background_color, Color.TRANSPARENT);
				if (bgColor == Color.TRANSPARENT) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						bgColor = context.getResources().getColor(R.color.av_color_bg, context.getTheme());
					} else {
						//noinspection deprecation
						bgColor = context.getResources().getColor(R.color.av_color_bg);
					}
				}
				int arrayId = array.getResourceId(R.styleable.GLAudioVisualizationView_av_waves_colors, 0);
				if (arrayId == 0 && !isInEditMode) {
					colors = array.getResources().getIntArray(R.array.av_colors);
				} else {
					if (isInEditMode && arrayId == 0)
						arrayId = R.array.av_colors;
					String[] colorsArray = array.getResources().getStringArray(arrayId);
					if (!Utils.allElementsAreNull(colorsArray)) {
						colors = new int[colorsArray.length];
						for (int i = 0; i < colors.length; i++) {
							colors[i] = Color.parseColor(colorsArray[i]);
						}
					} else {
						colors = context.getResources().getIntArray(arrayId);
					}
				}
			} finally {
				array.recycle();
			}
			if (colors.length < layersCount) {
				throw new IllegalArgumentException("You specified more layers than colors.");
			}

			layerColors = new float[colors.length][];
			for (int i = 0; i < colors.length; i++) {
				layerColors[i] = Utils.convertColor(colors[i]);
			}
			backgroundColor = Utils.convertColor(bgColor);
			bubbleSize /= context.getResources().getDisplayMetrics().widthPixels;
		}

		private Configuration(@NonNull Builder builder) {
			this.waveHeight = builder.waveHeight;
			waveHeight = Utils.between(waveHeight, Constants.MIN_WAVE_HEIGHT, Constants.MAX_WAVE_HEIGHT);
			this.wavesCount = builder.wavesCount;
			wavesCount = Utils.between(wavesCount, Constants.MIN_WAVES_COUNT, Constants.MAX_WAVES_COUNT);
			this.layerColors = builder.layerColors();
			this.bubbleSize = builder.bubbleSize;
			bubbleSize = Utils.between(bubbleSize, Constants.MIN_BUBBLE_SIZE, Constants.MAX_BUBBLE_SIZE);
			this.bubbleSize = this.bubbleSize / builder.context.getResources().getDisplayMetrics().widthPixels;
			this.footerHeight = builder.footerHeight;
			footerHeight = Utils.between(footerHeight, Constants.MIN_FOOTER_HEIGHT, Constants.MAX_FOOTER_HEIGHT);
			this.randomizeBubbleSize = builder.randomizeBubbleSize;
			this.backgroundColor = builder.backgroundColor();
			this.layersCount = builder.layersCount;
			layersCount = Utils.between(layersCount, Constants.MIN_LAYERS_COUNT, Constants.MAX_LAYERS_COUNT);
			if (layerColors.length < layersCount) {
				throw new IllegalArgumentException("You specified more layers than colors.");
			}
		}
	}

    public static class ColorsBuilder<T extends ColorsBuilder> {
        private float[] backgroundColor;
        private float[][] layerColors;
        private final Context context;

        public ColorsBuilder(@NonNull Context context) {
            this.context = context;
        }

        float[][] layerColors() {
            return layerColors;
        }

        float[] backgroundColor() {
            return backgroundColor;
        }

        /**
         * Set background color
         *
         * @param backgroundColor background color
         */
        public T setBackgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = Utils.convertColor(backgroundColor);
            return getThis();
        }

        /**
         * Set layer colors from array resource
         *
         * @param arrayId array resource
         */
        public T setLayerColors(@ArrayRes int arrayId) {
            String[] colorsArray = context.getResources().getStringArray(arrayId);
            int[] colors;
            if (!Utils.allElementsAreNull(colorsArray)) {
                colors = new int[colorsArray.length];
                for (int i = 0; i < colors.length; i++) {
                    colors[i] = Color.parseColor(colorsArray[i]);
                }
            } else {
                colors = context.getResources().getIntArray(arrayId);
            }
            return setLayerColors(colors);
        }

        /**
         * Set layer colors.
         *
         * @param colors array of colors
         */
        public T setLayerColors(int[] colors) {
            layerColors = new float[colors.length][];
            for (int i = 0; i < colors.length; i++) {
                layerColors[i] = Utils.convertColor(colors[i]);
            }
            return getThis();
        }

        /**
         * Set background color from color resource
         *
         * @param backgroundColor color resource
         */
        public T setBackgroundColorRes(@ColorRes int backgroundColor) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return setBackgroundColor(context.getResources().getColor(backgroundColor, context.getTheme()));
            //noinspection deprecation
            return setBackgroundColor(context.getResources().getColor(backgroundColor));
        }

        protected T getThis() {
            //noinspection unchecked
            return (T) this;
        }
    }

    public static class Builder extends ColorsBuilder<Builder> {

		private Context context;
		private int wavesCount;
		private int layersCount;
		private float bubbleSize;
		private float waveHeight;
		private float footerHeight;
		private boolean randomizeBubbleSize;

		public Builder(@NonNull Context context) {
            super(context);
			this.context = context;
		}

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
		 * Set waves count
		 *
		 * @param wavesCount waves count
		 */
		public Builder setWavesCount(int wavesCount) {
			this.wavesCount = wavesCount;
			return this;
		}

		/**
		 * Set layers count
		 *
		 * @param layersCount layers count
		 */
		public Builder setLayersCount(int layersCount) {
			this.layersCount = layersCount;
			return this;
		}

		/**
		 * Set bubbles size in pixels
		 *
		 * @param bubbleSize bubbles size in pixels
		 */
		public Builder setBubbleSize(float bubbleSize) {
			this.bubbleSize = bubbleSize;
			return this;
		}

		/**
		 * Set bubble size from dimension resource
		 *
		 * @param bubbleSize dimension resource
		 */
		public Builder setBubbleSize(@DimenRes int bubbleSize) {
			return setBubbleSize((float) context.getResources().getDimensionPixelSize(bubbleSize));
		}

		/**
		 * Set wave height in pixels
		 *
		 * @param waveHeight wave height in pixels
		 */
		public Builder setWaveHeight(float waveHeight) {
			this.waveHeight = waveHeight;
			return this;
		}

		/**
		 * Set wave height from dimension resource
		 *
		 * @param waveHeight dimension resource
		 */
		public Builder setWaveHeight(@DimenRes int waveHeight) {
			return setWaveHeight((float) context.getResources().getDimensionPixelSize(waveHeight));
		}

		/**
		 * Set footer height in pixels
		 *
		 * @param footerHeight footer height in pixels
		 */
		public Builder setFooterHeight(float footerHeight) {
			this.footerHeight = footerHeight;
			return this;
		}

		/**
		 * Set footer height from dimension resource
		 *
		 * @param footerHeight dimension resource
		 */
		public Builder setFooterHeight(@DimenRes int footerHeight) {
			return setFooterHeight((float) context.getResources().getDimensionPixelSize(footerHeight));
		}

		/**
		 * Set flag indicates that size of bubbles should be randomized
		 *
		 * @param randomizeBubbleSize true if size of bubbles should be randomized, false if size of bubbles must be the same
		 */
		public Builder setRandomizeBubbleSize(boolean randomizeBubbleSize) {
			this.randomizeBubbleSize = randomizeBubbleSize;
			return this;
		}

		public GLAudioVisualizationView build() {
			return new GLAudioVisualizationView(this);
		}
	}

    /**
     * Renderer builder.
     */
    public static class RendererBuilder {

        private final Builder builder;
        private GLSurfaceView glSurfaceView;
        private DbmHandler handler;

        /**
         * Create new renderer using existing Audio Visualization builder.
         * @param builder instance of Audio Visualization builder
         */
        public RendererBuilder(@NonNull Builder builder) {
            this.builder = builder;
        }

        /**
         * Set dBm handler.
         * @param handler instance of dBm handler
         */
        public RendererBuilder handler(DbmHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Set OpenGL surface view.
         * @param glSurfaceView instance of OpenGL surface view
         */
        public RendererBuilder glSurfaceView(@NonNull GLSurfaceView glSurfaceView) {
            this.glSurfaceView = glSurfaceView;
            return this;
        }

        /**
         * Create new Audio Visualization Renderer.
         * @return new Audio Visualization Renderer
         */
        public AudioVisualizationRenderer build() {
            final GLRenderer renderer = new GLRenderer(builder.context, new Configuration(builder));
            final InnerAudioVisualization audioVisualization = new InnerAudioVisualization() {
                @Override
                public void startRendering() {
                    if (glSurfaceView.getRenderMode() != RENDERMODE_CONTINUOUSLY) {
                        glSurfaceView.setRenderMode(RENDERMODE_CONTINUOUSLY);
                    }
                }

                @Override
                public void stopRendering() {
                    if (glSurfaceView.getRenderMode() != RENDERMODE_WHEN_DIRTY) {
                        glSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
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
            handler.setUp(audioVisualization, builder.layersCount);
            return renderer;
        }
    }

    /**
     * Audio Visualization renderer interface that allows to change waves' colors at runtime.
     */
    public interface AudioVisualizationRenderer extends Renderer {

        /**
         * Update colors configuration.
         * @param builder instance of color builder.
         */
        void updateConfiguration(@NonNull ColorsBuilder builder);
    }
}

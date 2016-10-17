package com.cleveroad.audiovisualization;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wave layer implementation.
 */
class GLWaveLayer {

    private final GLBubble[] mAllBubbles;
    private final float mBubbleFromY;
    private final float mBubbleToY;
    private final GLAudioVisualizationView.Configuration mConfiguration;
    private final Set<GLBubble> mProducedBubbles;
    private final Random mRandom;
    private final GLRectangle mGlRectangle;
    private final Queue<GLBubble> mUnusedBubbles;
    private final Set<GLBubble> mUsedBubbles;
    private final GLWave[] mWaves;
    private float mAmplitude;
    private boolean mCalmedDown;


    GLWaveLayer(GLAudioVisualizationView.Configuration configuration, float[] color, float fromY, float toY, Random random) {
        mConfiguration = configuration;
        mRandom = random;
        mWaves = new GLWave[configuration.mWavesCount];
        float footerToY = fromY + configuration.mFooterHeight / (configuration.mFooterHeight + configuration.mWaveHeight * 2) * (toY - fromY);
        mGlRectangle = new GLRectangle(color, -1, 1, fromY, footerToY);
        float waveWidth = 2f / configuration.mWavesCount;
        float[] points = randomPoints(mRandom, configuration.mWavesCount, waveWidth, 0.15f);
        mBubbleFromY = footerToY;
        mBubbleToY = toY;
        for (int i = 0; i < configuration.mWavesCount; i++) {
            byte direction = i % 2 == 0 ? GLWave.DIRECTION_UP : GLWave.DIRECTION_DOWN;
            mWaves[i] = new GLWave(color, points[i], points[i + 1], footerToY, toY, direction, random);
        }
        mUsedBubbles = Collections.newSetFromMap(new ConcurrentHashMap<GLBubble, Boolean>());
        mProducedBubbles = Collections.newSetFromMap(new ConcurrentHashMap<GLBubble, Boolean>());
        mUnusedBubbles = new LinkedList<>();
        mAllBubbles = generateBubbles(color, configuration.mBubblesPerLayer);
        Collections.addAll(mUnusedBubbles, mAllBubbles);
    }

    /**
     * Draw whole wave layer.
     */
    void draw() {
        for (GLWave wave : mWaves) {
            wave.draw();
        }
        mGlRectangle.draw();
        for (GLBubble bubble : mUsedBubbles) {
            bubble.draw();
        }
    }

    boolean isCalmedDown() {
        return mCalmedDown;
    }

    public void setColor(float[] color) {
        mGlRectangle.setColor(color);
        for (GLWave wave : mWaves) {
            wave.setColor(color);
        }
        for (GLBubble bubble : mAllBubbles) {
            bubble.setColor(color);
        }
    }

    /**
     * Update waves and bubbles positions.
     *
     * @param dt     time elapsed from last calculations
     * @param dAngle delta angle
     * @param ratioY aspect ratio for Y coordinates
     */
    void update(long dt, float dAngle, float ratioY) {
        float d = dt * dAngle;
        mCalmedDown = true;
        for (GLWave wave : mWaves) {
            wave.update(d);
            mCalmedDown &= wave.isCalmedDown();
        }
        mUsedBubbles.addAll(mProducedBubbles);
        mProducedBubbles.clear();
        Iterator<GLBubble> iterator = mUsedBubbles.iterator();
        while (iterator.hasNext()) {
            GLBubble bubble = iterator.next();
            bubble.update(dt, ratioY);
            if (bubble.isOffScreen()) {
                mUnusedBubbles.add(bubble);
                iterator.remove();
            }
        }
    }

    /**
     * Update waves data.
     *
     * @param heightCoefficient wave height's coefficient
     * @param amplitude         amplitude
     */
    void updateData(float heightCoefficient, float amplitude) {
        for (GLWave wave : mWaves) {
            wave.setCoefficient(Utils.randomize(heightCoefficient, mRandom));
        }
        if (amplitude > mAmplitude) {
            mAmplitude = amplitude;
            if (heightCoefficient > 0.25f) {
                produceBubbles();
            }
        } else {
            mAmplitude = Utils.smooth(mAmplitude, amplitude, 0.8f);
        }
    }

    /**
     * Generate bubbles.
     *
     * @param color color of bubbles
     * @param count number of bubbles to generate
     * @return generated bubbles
     */
    private GLBubble[] generateBubbles(float[] color, int count) {
        GLBubble[] bubbles = new GLBubble[count];
        for (int i = 0; i < count; i++) {
            float size = mConfiguration.mBubbleSize;
            if (mConfiguration.mRandomizeBubbleSize) {
                size *= 0.5f + mRandom.nextFloat() * 0.8f;
            }
            float shift = mRandom.nextFloat() * 0.1f * (mRandom.nextBoolean() ? 1 : -1);
            float[] col = new float[color.length];
            System.arraycopy(color, 0, col, 0, col.length);
            bubbles[i] = new GLBubble(col, -1 + mRandom.nextFloat() * 2, mBubbleFromY + shift, mBubbleToY, size, mRandom);
        }
        return bubbles;
    }

    /**
     * Produce new bubbles.
     */
    private void produceBubbles() {
        int bubblesCount = mRandom.nextInt(3);
        for (int i = 0; i < bubblesCount; i++) {
            GLBubble bubble = mUnusedBubbles.poll();
            if (bubble != null) {
                float shift = mRandom.nextFloat() * 0.1f * (mRandom.nextBoolean() ? 1 : -1);
                float size = mConfiguration.mBubbleSize;
                if (mConfiguration.mRandomizeBubbleSize) {
                    size *= 0.5f + mRandom.nextFloat() * 0.8f;
                }
                bubble.update(-1 + mRandom.nextFloat() * 2, mBubbleFromY + shift, mBubbleToY, size);
                mProducedBubbles.add(bubble);
            }
        }
    }

    /**
     * Generate random points for wave.
     *
     * @param random     instance of Random
     * @param wavesCount number of waves
     * @param width      width of single wave
     * @param shiftCoef  shift coefficient
     * @return generated points for waves
     */
    private static float[] randomPoints(Random random, int wavesCount, float width, float shiftCoef) {
        float shift;
        float[] points = new float[wavesCount + 1];
        for (int i = 0; i < points.length; i++) {
            if (i == 0) {
                points[i] = -1;
            } else if (i == points.length - 1) {
                points[i] = 1;
            } else {
                shift = random.nextFloat() * shiftCoef * width;
                shift *= random.nextBoolean() ? 1 : -1;
                points[i] = -1 + i * width + shift;
            }
        }
        return points;
    }
}

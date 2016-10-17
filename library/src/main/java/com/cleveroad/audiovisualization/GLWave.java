package com.cleveroad.audiovisualization;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

/**
 * Single wave implementation.
 */
class GLWave extends GLShape {

    /**
     * Wave movement from top to bottom.
     */
    static final byte DIRECTION_DOWN = 1;
    /**
     * Wave movement from bottom to top.
     */
    static final byte DIRECTION_UP = 0;
    /**
     * Number of additional points used for drawing wave: center, lb, lt, rt, rb.
     */
    private static final int ADDITIONAL_POINTS = 5;
    /**
     * Number of points used for drawing Bezier curve.
     */
    private static final int POINTS_PER_WAVE = 40;
    /**
     * Number of points to skip for getting proper index for Bezier curve points.
     */
    private static final int SKIP = (int) Math.ceil(ADDITIONAL_POINTS / 2f) * COORDS_PER_VERTEX;
    /**
     * Smooth coefficient for {@link Utils#smooth(float, float, float)} method.
     */
    private static final float SMOOTH_A = 0.35f;
    private final float mFromX, mToX;
    private final float mFromY, mToY;
    private final Random mRandom;
    private float mWaveX = 0;
    private float mCoefficient;
    private float mCurrentAngle;
    private float mLatestCoefficient;
    private float mPrevVal;
    private ShortBuffer mShortBuffer;
    private FloatBuffer mVertexBuffer;
    private float[] mVertices;

    GLWave(float[] color, float fromX, float toX, float fromY, float toY, byte direction, Random random) {
        super(color);
        mFromX = fromX;
        mToX = toX;
        mFromY = fromY;
        mToY = toY;
        mRandom = random;
        mCurrentAngle = direction == DIRECTION_UP ? 0 : (float) Math.PI;
        initVertices();
        initIndices();
    }

    /**
     * Draw wave.
     */
    void draw() {
        GLES20.glUseProgram(getProgram());
        int positionHandle = GLES20.glGetAttribLocation(getProgram(), VERTEX_POSITION);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * SIZE_OF_FLOAT, mVertexBuffer);
        int colorHandle = GLES20.glGetUniformLocation(getProgram(), VERTEX_COLOR);
        GLES20.glUniform4fv(colorHandle, 1, getColor(), 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mShortBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, mShortBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    boolean isCalmedDown() {
        return Math.abs(mPrevVal) < 0.001f;
    }

    /**
     * Set wave height coefficient.
     *
     * @param coefficient wave height coefficient
     */
    void setCoefficient(float coefficient) {
        mLatestCoefficient = coefficient;
    }

    /**
     * Update wave position.
     *
     * @param dAngle delta angle
     */
    void update(float dAngle) {
        if (mVertexBuffer == null) {
            ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(mVertices.length * SIZE_OF_FLOAT);
            vertexByteBuffer.order(ByteOrder.nativeOrder());
            mVertexBuffer = vertexByteBuffer.asFloatBuffer();
            mVertexBuffer.put(mVertices);
            mVertexBuffer.position(0);
        }
        float angle = mCurrentAngle;
        angle += dAngle;
        mCurrentAngle = angle;
        if (mCoefficient == 0 && mLatestCoefficient > 0) {
            mCoefficient = Utils.smooth(0, mLatestCoefficient, SMOOTH_A);
        }

        float val = (float) Math.sin(angle) * mCoefficient;
        if (mPrevVal > 0 && val <= 0 || mPrevVal < 0 && val >= 0) {
            mCoefficient = Utils.smooth(mCoefficient, mLatestCoefficient, SMOOTH_A);
            mWaveX = mRandom.nextFloat() * 0.3f * (mRandom.nextBoolean() ? 1 : -1);
        }
        mPrevVal = val;
        int i = 0;
        double step = 1.0 / POINTS_PER_WAVE;
        float posX = Utils.normalizeGl(mWaveX, mFromX, mToX);
        float posY = Utils.normalizeGl(val, mFromY, mToY);
        for (float time = 0; time < 1 - step / 2; time += step) {
            mVertices[COORDS_PER_VERTEX * i + 1 + SKIP] = angle;
            mVertexBuffer.put(COORDS_PER_VERTEX * i + SKIP, Utils.quad(time, mVertices[6], posX, mVertices[mVertices.length - 6]));
            mVertexBuffer.put(COORDS_PER_VERTEX * i + 1 + SKIP, Utils.quad(time, mVertices[7], posY, mVertices[mVertices.length - 5]));
            i++;
        }
    }

    private void initIndices() {
        short[] indices = new short[(POINTS_PER_WAVE + ADDITIONAL_POINTS - 2) * COORDS_PER_VERTEX];
        for (int i = 0; i < indices.length / COORDS_PER_VERTEX; i++) {
            indices[COORDS_PER_VERTEX * i] = 0;
            indices[COORDS_PER_VERTEX * i + 1] = (short) (i + 1);
            indices[COORDS_PER_VERTEX * i + 2] = (short) (i + 2);
        }
        ByteBuffer indicesByteBuffer = ByteBuffer.allocateDirect(indices.length * SIZE_OF_SHORT);
        indicesByteBuffer.order(ByteOrder.nativeOrder());
        mShortBuffer = indicesByteBuffer.asShortBuffer();
        mShortBuffer.put(indices);
        mShortBuffer.position(0);
    }

    private void initVertices() {
        int items = POINTS_PER_WAVE + ADDITIONAL_POINTS;
        int size = items * COORDS_PER_VERTEX;
        mVertices = new float[size];

        // center
        mVertices[0] = Utils.normalizeGl(0f, mFromX, mToX);
        mVertices[1] = Utils.normalizeGl(-1f, mFromY, mToY);

        // left bottom footer
        mVertices[3] = Utils.normalizeGl(-1f, mFromX, mToX);
        mVertices[4] = Utils.normalizeGl(-1f, mFromY, mToY);

        // left top footer
        mVertices[6] = mVertices[3];
        mVertices[7] = Utils.normalizeGl(0f, mFromY, mToY);

        // right top footer
        mVertices[mVertices.length - 6] = Utils.normalizeGl(1f, mFromX, mToX);
        mVertices[mVertices.length - 5] = mVertices[7];

        // right bottom footer
        mVertices[mVertices.length - 3] = mVertices[mVertices.length - 6];
        mVertices[mVertices.length - 2] = mVertices[4];
    }
}

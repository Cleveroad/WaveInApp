package com.cleveroad.audiovisualization;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

/**
 * Bubble implementation.
 */
class GLBubble extends GLShape {

    /**
     * Duration of bubble movement.
     */
    private static final long BUBBLE_ANIMATION_DURATION = 1000;
    private static final float BUBBLE_D_ANGLE = (float) (2 * Math.PI / BUBBLE_ANIMATION_DURATION);

    /**
     * Number of points for drawing circle.
     */
    private static final int POINTS_PER_CIRCLE = 40;
    private static final float TOP_Y = 1f;
    
    private final Random mRandom;
    private final ShortBuffer mShortBuffer;
    private final FloatBuffer mVertexBuffer;
    private float mAngle;
    private float mCenterY = -1;
    private float mFromY;
    private float mSize;
    private float mSpeed;
    private float mStartX;
    private float mVirtualSpeed;

    GLBubble(float[] color, float startX, float fromY, float toY, float size, Random random) {
        super(color);
        mRandom = random;
        update(startX, fromY, toY, size);
        float[] vertices = new float[(POINTS_PER_CIRCLE + 1) * COORDS_PER_VERTEX];
        short[] indices = new short[POINTS_PER_CIRCLE * COORDS_PER_VERTEX];
        int i;
        for (i = 0; i < indices.length / COORDS_PER_VERTEX - 1; i++) {
            indices[COORDS_PER_VERTEX * i] = 0;
            indices[COORDS_PER_VERTEX * i + 1] = (short) (i + 1);
            indices[COORDS_PER_VERTEX * i + 2] = (short) (i + 2);
        }
        // connect first and last elements
        indices[COORDS_PER_VERTEX * i] = 0;
        indices[COORDS_PER_VERTEX * i + 1] = (short) (i + 1);
        indices[COORDS_PER_VERTEX * i + 2] = (short) 1;
        ByteBuffer verticesByteBuffer = ByteBuffer.allocateDirect(vertices.length * SIZE_OF_FLOAT);
        verticesByteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = verticesByteBuffer.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        ByteBuffer indicesByteBuffer = ByteBuffer.allocateDirect(indices.length * SIZE_OF_SHORT);
        indicesByteBuffer.order(ByteOrder.nativeOrder());
        mShortBuffer = indicesByteBuffer.asShortBuffer();
        mShortBuffer.put(indices);
        mShortBuffer.position(0);
        mAngle = (float) (random.nextFloat() * 2 * Math.PI);
    }

    /**
     * Draw bubble.
     */
    void draw() {
        GLES20.glUseProgram(getProgram());
        int positionHandle = GLES20.glGetAttribLocation(getProgram(), VERTEX_POSITION);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * SIZE_OF_FLOAT, mVertexBuffer);
        int colorHandle = GLES20.glGetUniformLocation(getProgram(), VERTEX_COLOR);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUniform4fv(colorHandle, 1, getColor(), 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mShortBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, mShortBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    /**
     * Check if bubble is moved out of specified area.
     *
     * @return true if bubble is outside of specified area, false otherwise
     */
    boolean isOffScreen() {
        return mCenterY > TOP_Y;
    }

    /**
     * Update position of bubble.
     *
     * @param dt     time elapsed from last calculations
     * @param ratioY aspect ratio for Y coordinates
     */
    void update(long dt, float ratioY) {
        double step = 2 * Math.PI / POINTS_PER_CIRCLE;
        mAngle += dt * BUBBLE_D_ANGLE;
        float fromX = mStartX + (float) (0.05f * Math.sin(mAngle));
        float toX = fromX + mSize;
        float fromY = mFromY + dt * mSpeed;
        float toY = fromY + mSize;
        mCenterY += dt * mVirtualSpeed;
        getColor()[3] = (TOP_Y - mCenterY / TOP_Y);
        mVertexBuffer.put(0, Utils.normalizeGl(0, fromX, toX));
        mVertexBuffer.put(1, Utils.normalizeGl(mCenterY * ratioY, fromY, toY));
        for (int i = 1; i <= POINTS_PER_CIRCLE; i++) {
            mVertexBuffer.put(COORDS_PER_VERTEX * i, Utils.normalizeGl((float) Math.sin(-Math.PI + step * i), fromX, toX));
            mVertexBuffer.put(COORDS_PER_VERTEX * i + 1, Utils.normalizeGl((float) Math.cos(-Math.PI + step * i) * ratioY, fromY, toY));
        }
        mFromY = fromY;
    }

    /**
     * Update bubble's area of movement.
     *
     * @param startX start X position
     * @param fromY  start Y position
     * @param toY    end Y position
     * @param size   size of bubble
     */
    void update(float startX, float fromY, float toY, float size) {
        mFromY = fromY;
        mSize = size;
        mStartX = startX;
        mCenterY = -1;
        float coef = 0.4f + mRandom.nextFloat() * 0.8f; // randomize mSpeed of movement
        mSpeed = (toY - fromY) / BUBBLE_ANIMATION_DURATION * coef;
        mVirtualSpeed = 2f / BUBBLE_ANIMATION_DURATION * coef;
        getColor()[3] = 1f;
    }
}

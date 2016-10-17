package com.cleveroad.audiovisualization;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Rectangle implementation.
 */
class GLRectangle extends GLShape {

    private final ShortBuffer mShortBuffer;
    private final FloatBuffer mVertexBuffer;

    GLRectangle(float[] color, float fromX, float toX, float fromY, float toY) {
        super(color);
        final float[] vertices = {
                Utils.normalizeGl(-1, fromX, toX), Utils.normalizeGl(1, fromY, toY), 0,
                Utils.normalizeGl(-1, fromX, toX), Utils.normalizeGl(-1, fromY, toY), 0,
                Utils.normalizeGl(1, fromX, toX), Utils.normalizeGl(-1, fromY, toY), 0,
                Utils.normalizeGl(1, fromX, toX), Utils.normalizeGl(1, fromY, toY), 0
        };
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * SIZE_OF_FLOAT);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = vertexByteBuffer.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        final short[] indices = {0, 1, 2, 0, 2, 3};
        ByteBuffer indicesByteBuffer = ByteBuffer.allocateDirect(indices.length * SIZE_OF_SHORT);
        indicesByteBuffer.order(ByteOrder.nativeOrder());
        mShortBuffer = indicesByteBuffer.asShortBuffer();
        mShortBuffer.put(indices);
        mShortBuffer.position(0);
    }

    /**
     * Draw rectangle.
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
}

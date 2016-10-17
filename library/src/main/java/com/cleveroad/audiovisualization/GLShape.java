package com.cleveroad.audiovisualization;

import android.opengl.GLES20;

/**
 * Abstract shape implementation.
 */
abstract class GLShape {

    static final int COORDS_PER_VERTEX = 3;
    static final int SIZE_OF_FLOAT = 4;
    static final int SIZE_OF_SHORT = 2;
    static final String VERTEX_COLOR = "vColor";
    static final String VERTEX_POSITION = "vPosition";

    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
                    "uniform vec4 " + VERTEX_COLOR + ";" +
                    "void main() {" +
                    "  gl_FragColor = " + VERTEX_COLOR + ";" +
                    "}";
    private static final String VERTEX_SHADER_CODE =
            "attribute vec4 " + VERTEX_POSITION + ";" +
                    "void main() {" +
                    "  gl_Position = " + VERTEX_POSITION + ";" +
                    "}";
    /**
     * Shape color.
     */
    private final float mColor[];

    /**
     * Program associated with shape.
     */
    private final int mProgram;

    GLShape(float[] color) {
        mColor = color;
        int vertexShader = GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
        int fragmentShader = GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    protected float[] getColor() {
        return mColor;
    }

    public void setColor(float[] color) {
        System.arraycopy(color, 0, mColor, 0, mColor.length);
    }

    int getProgram() {
        return mProgram;
    }
}

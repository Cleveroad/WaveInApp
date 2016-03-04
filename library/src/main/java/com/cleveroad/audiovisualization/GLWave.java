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
	 * Wave movement from bottom to top.
	 */
	public static final byte DIRECTION_UP = 0;
	/**
	 * Wave movement from top to bottom.
	 */
	public static final byte DIRECTION_DOWN = 1;
	/**
	 * Smooth coefficient for {@link Utils#smooth(float, float, float)} method.
	 */
	private static final float SMOOTH_A = 0.35f;

	/**
	 * Number of points used for drawing Bezier curve.
	 */
	private static final int POINTS_PER_WAVE = 40;

	/**
	 * Number of additional points used for drawing wave: center, lb, lt, rt, rb.
	 */
	private static final int ADDITIONAL_POINTS = 5;

	/**
	 * Number of points to skip for getting proper index for Bezier curve points.
	 */
	private static final int SKIP = (int) Math.ceil(ADDITIONAL_POINTS / 2f) * COORDS_PER_VERTEX;

    private FloatBuffer vertexBuffer;
	private ShortBuffer shortBuffer;
	private final Random random;
	private final float fromX, toX;
	private final float fromY, toY;
	private float[] vertices;
	private float currentAngle;
	private float coefficient;
	private float latestCoefficient;
	private float prevVal;

	public GLWave(float[] color, float fromX, float toX, float fromY, float toY, byte direction, Random random) {
		super(color);
		this.fromX = fromX;
		this.toX = toX;
		this.fromY = fromY;
		this.toY = toY;
		this.random = random;
		currentAngle = direction == DIRECTION_UP ? 0 : (float) Math.PI;
		initVertices();
		initIndices();
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
		shortBuffer = indicesByteBuffer.asShortBuffer();
		shortBuffer.put(indices);
		shortBuffer.position(0);
	}

	private void initVertices() {
		int items = POINTS_PER_WAVE + ADDITIONAL_POINTS;
		int size = items * COORDS_PER_VERTEX;
		vertices = new float[size];

		// center
		vertices[0] = Utils.normalizeGl(0f, fromX, toX);
		vertices[1] = Utils.normalizeGl(-1f, fromY, toY);

		// left bottom footer
		vertices[3] = Utils.normalizeGl(-1f, fromX, toX);
		vertices[4] = Utils.normalizeGl(-1f, fromY, toY);

		// left top footer
		vertices[6] = vertices[3];
		vertices[7] = Utils.normalizeGl(0f, fromY, toY);

		// right top footer
		vertices[vertices.length - 6] = Utils.normalizeGl(1f, fromX, toX);
		vertices[vertices.length - 5] = vertices[7];

		// right bottom footer
		vertices[vertices.length - 3] = vertices[vertices.length - 6];
		vertices[vertices.length - 2] = vertices[4];
	}

	float waveX = 0;
	/**
	 * Update wave position.
	 * @param dAngle delta angle
	 */
	public void update(float dAngle) {
		if (vertexBuffer == null) {
			ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * SIZE_OF_FLOAT);
			vertexByteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffer = vertexByteBuffer.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);
		}
		float angle = currentAngle;
		angle += dAngle;
		currentAngle = angle;
		if (coefficient == 0 && latestCoefficient > 0) {
			coefficient = Utils.smooth(0, latestCoefficient, SMOOTH_A);
		}

		float val = (float) Math.sin(angle) * coefficient;
		if (prevVal > 0 && val <= 0 || prevVal < 0 && val >= 0) {
			coefficient = Utils.smooth(coefficient, latestCoefficient, SMOOTH_A);
			waveX = random.nextFloat() * 0.3f * (random.nextBoolean() ? 1 : -1);
		}
		prevVal = val;
		int i = 0;
		double step = 1.0 / POINTS_PER_WAVE;
		float posX = Utils.normalizeGl(waveX, fromX, toX);
		float posY = Utils.normalizeGl(val, fromY, toY);
        for (float time = 0; time < 1 - step / 2; time += step) {
			vertices[COORDS_PER_VERTEX * i + 1 + SKIP] = angle;
			vertexBuffer.put(COORDS_PER_VERTEX * i + SKIP, Utils.quad(time, vertices[6], posX, vertices[vertices.length - 6]));
			vertexBuffer.put(COORDS_PER_VERTEX * i + 1 + SKIP, Utils.quad(time, vertices[7], posY, vertices[vertices.length - 5]));
			i++;
		}
	}

    public boolean isCalmedDown() {
        return Math.abs(prevVal) < 0.001f;
    }

	/**
	 * Set wave height coefficient.
	 * @param coefficient wave height coefficient
	 */
	public void setCoefficient(float coefficient) {
		this.latestCoefficient = coefficient;
	}

	/**
	 * Draw wave.
	 */
	public void draw() {
		GLES20.glUseProgram(getProgram());
		int positionHandle = GLES20.glGetAttribLocation(getProgram(), VERTEX_POSITION);
		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * SIZE_OF_FLOAT, vertexBuffer);
		int colorHandle = GLES20.glGetUniformLocation(getProgram(), VERTEX_COLOR);
		GLES20.glUniform4fv(colorHandle, 1, getColor(), 0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, shortBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, shortBuffer);
		GLES20.glDisableVertexAttribArray(positionHandle);
	}
}

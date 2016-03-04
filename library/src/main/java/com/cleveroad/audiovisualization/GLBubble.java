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
	private final FloatBuffer vertexBuffer;
	private final ShortBuffer shortBuffer;
	private final Random random;
	private float fromY;
	private float size;
	private float speed;
	private float virtualSpeed;
	private float centerY = -1;
	private float startX;
	private float angle;

	public GLBubble(float[] color, float startX, float fromY, float toY, float size, Random random) {
		super(color);
		this.random = random;
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
		vertexBuffer = verticesByteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		ByteBuffer indicesByteBuffer = ByteBuffer.allocateDirect(indices.length * SIZE_OF_SHORT);
		indicesByteBuffer.order(ByteOrder.nativeOrder());
		shortBuffer = indicesByteBuffer.asShortBuffer();
		shortBuffer.put(indices);
		shortBuffer.position(0);
		angle = (float) (random.nextFloat() * 2 * Math.PI);
	}

	/**
	 * Update position of bubble.
	 * @param dt time elapsed from last calculations
	 * @param ratioY aspect ratio for Y coordinates
	 */
	public void update(long dt, float ratioY) {
		double step = 2 * Math.PI / POINTS_PER_CIRCLE;
		angle += dt * BUBBLE_D_ANGLE;
		float fromX = startX + (float) (0.05f * Math.sin(angle));
		float toX = fromX + size;
		float fromY = this.fromY + dt * speed;
		float toY = fromY + size;
		centerY += dt * virtualSpeed;
		getColor()[3] = (TOP_Y - centerY / TOP_Y);
		vertexBuffer.put(0, Utils.normalizeGl(0, fromX, toX));
		vertexBuffer.put(1, Utils.normalizeGl(centerY * ratioY, fromY, toY));
		for (int i=1; i<=POINTS_PER_CIRCLE; i++) {
			vertexBuffer.put(COORDS_PER_VERTEX * i, Utils.normalizeGl((float) Math.sin(-Math.PI + step * i), fromX, toX));
			vertexBuffer.put(COORDS_PER_VERTEX * i + 1, Utils.normalizeGl((float) Math.cos(-Math.PI + step * i) * ratioY, fromY, toY));
		}
		this.fromY = fromY;
	}

	/**
	 * Draw bubble.
	 */
	public void draw() {
		GLES20.glUseProgram(getProgram());
		int positionHandle = GLES20.glGetAttribLocation(getProgram(), VERTEX_POSITION);
		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * SIZE_OF_FLOAT, vertexBuffer);
		int colorHandle = GLES20.glGetUniformLocation(getProgram(), VERTEX_COLOR);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glUniform4fv(colorHandle, 1, getColor(), 0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, shortBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, shortBuffer);
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisable(GLES20.GL_BLEND);
	}

	/**
	 * Check if bubble is moved out of specified area.
	 * @return true if bubble is outside of specified area, false otherwise
	 */
	public boolean isOffScreen() {
		return centerY > TOP_Y;
	}

	/**
	 * Update bubble's area of movement.
	 * @param startX start X position
	 * @param fromY start Y position
	 * @param toY end Y position
	 * @param size size of bubble
	 */
	public void update(float startX, float fromY, float toY, float size) {
		this.fromY = fromY;
		this.size = size;
		this.startX = startX;
		this.centerY = -1;
		float coef = 0.4f + random.nextFloat() * 0.8f; // randomize speed of movement
		this.speed = (toY - fromY) / BUBBLE_ANIMATION_DURATION * coef;
		this.virtualSpeed = 2f / BUBBLE_ANIMATION_DURATION * coef;
		getColor()[3] = 1f;
	}
}

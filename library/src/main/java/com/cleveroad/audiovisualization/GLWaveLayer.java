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

	private final GLAudioVisualizationView.Configuration configuration;
	private final GLWave[] waves;
	private final GLRectangle rectangle;
	private final Random random;
	private final float bubbleFromY;
	private final float bubbleToY;
	private float amplitude;

	private final Set<GLBubble> usedBubbles;
	private final Queue<GLBubble> unusedBubbles;
	private final Set<GLBubble> producedBubbles;
    private boolean isCalmedDown;
    private final GLBubble[] allBubbles;


	public GLWaveLayer(GLAudioVisualizationView.Configuration configuration, float[] color, float fromY, float toY, Random random) {
		this.configuration = configuration;
		this.random = random;
		this.waves = new GLWave[configuration.wavesCount];
		float footerToY = fromY + configuration.footerHeight / (configuration.footerHeight + configuration.waveHeight * 2) * (toY - fromY);
		this.rectangle = new GLRectangle(color, -1, 1, fromY, footerToY);
		float waveWidth = 2f / configuration.wavesCount;
		float[] points = randomPoints(this.random, configuration.wavesCount, waveWidth, 0.15f);
		this.bubbleFromY = footerToY;
		this.bubbleToY = toY;
		for (int i = 0; i < configuration.wavesCount; i++) {
			byte direction = i % 2 == 0 ? GLWave.DIRECTION_UP : GLWave.DIRECTION_DOWN;
			waves[i] = new GLWave(color, points[i], points[i + 1], footerToY, toY, direction, random);
		}
        this.usedBubbles = Collections.newSetFromMap(new ConcurrentHashMap<GLBubble, Boolean>());
        this.producedBubbles = Collections.newSetFromMap(new ConcurrentHashMap<GLBubble, Boolean>());
        this.unusedBubbles = new LinkedList<>();
        allBubbles = generateBubbles(color, configuration.bubblesPerLayer);
		Collections.addAll(unusedBubbles, allBubbles);
	}

    /**
	 * Generate random points for wave.
	 * @param random instance of Random
	 * @param wavesCount number of waves
	 * @param width width of single wave
	 * @param shiftCoef shift coefficient
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


	/**
	 * Update waves and bubbles positions.
	 * @param dt time elapsed from last calculations
	 * @param dAngle delta angle
	 * @param ratioY aspect ratio for Y coordinates
	 */
	public void update(long dt, float dAngle, float ratioY) {
		float d = dt * dAngle;
        isCalmedDown = true;
		for (GLWave wave : waves) {
			wave.update(d);
            isCalmedDown &= wave.isCalmedDown();
		}
        usedBubbles.addAll(producedBubbles);
		producedBubbles.clear();
		Iterator<GLBubble> iterator = usedBubbles.iterator();
		while (iterator.hasNext()){
			GLBubble bubble = iterator.next();
			bubble.update(dt, ratioY);
			if (bubble.isOffScreen()) {
				unusedBubbles.add(bubble);
				iterator.remove();
			}
		}
	}

    public boolean isCalmedDown() {
        return isCalmedDown;
    }

    /**
	 * Draw whole wave layer.
	 */
	public void draw() {
		for (GLWave wave : waves) {
			wave.draw();
		}
		rectangle.draw();
		for (GLBubble bubble : usedBubbles) {
			bubble.draw();
		}
	}

	/**
	 * Update waves data.
	 * @param heightCoefficient wave height's coefficient
	 * @param amplitude amplitude
	 */
	public void updateData(float heightCoefficient, float amplitude) {
		for (GLWave wave : waves) {
			wave.setCoefficient(Utils.randomize(heightCoefficient, random));
		}
		if (amplitude > this.amplitude) {
			this.amplitude = amplitude;
			if (heightCoefficient > 0.25f) {
				produceBubbles();
			}
		} else {
			this.amplitude = Utils.smooth(this.amplitude, amplitude, 0.8f);
		}
	}

	/**
	 * Produce new bubbles.
	 */
	private void produceBubbles() {
		int bubblesCount = random.nextInt(3);
		for (int i = 0; i < bubblesCount; i++) {
			GLBubble bubble = unusedBubbles.poll();
			if (bubble != null) {
				float shift = random.nextFloat() * 0.1f * (random.nextBoolean() ? 1 : -1);
				float size = configuration.bubbleSize;
				if (configuration.randomizeBubbleSize) {
					size *= 0.5f + random.nextFloat() * 0.8f;
				}
				bubble.update(-1 + random.nextFloat() * 2, bubbleFromY + shift, bubbleToY, size);
				producedBubbles.add(bubble);
			}
		}
	}

	/**
	 * Generate bubbles.
	 * @param color color of bubbles
	 * @param count number of bubbles to generate
	 * @return generated bubbles
	 */
	private GLBubble[] generateBubbles(float[] color, int count) {
		GLBubble[] bubbles = new GLBubble[count];
		for (int i=0; i<count; i++) {
			float size = configuration.bubbleSize;
			if (configuration.randomizeBubbleSize) {
				size *= 0.5f + random.nextFloat() * 0.8f;
			}
			float shift = random.nextFloat() * 0.1f * (random.nextBoolean() ? 1 : -1);
			float[] col = new float[color.length];
			System.arraycopy(color, 0, col, 0, col.length);
			bubbles[i] = new GLBubble(col, -1 + random.nextFloat() * 2, bubbleFromY + shift, bubbleToY, size, random);
		}
		return bubbles;
	}

    public void setColor(float[] color) {
        rectangle.setColor(color);
        for (GLWave wave : waves) {
            wave.setColor(color);
        }
        for (GLBubble bubble : allBubbles) {
            bubble.setColor(color);
        }
    }
}

package com.cleveroad.audiovisualization;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Random;

/**
 * Helpful utils.
 */
class Utils {
	private Utils() {}

	/**
	 * Check if value within allowed range.
	 * @param val some value
	 * @param min minimum value
	 * @param max maximum value
	 */
	public static float between(float val, float min, float max) {
		return Math.max(Math.min(val, max), min);
	}

	/**
	 * Check if value within allowed range.
	 * @param val some value
	 * @param min minimum value
	 * @param max maximum value
	 */
	public static int between(int val, int min, int max) {
		return Math.max(Math.min(val, max), min);
	}

	/**
	 * Convert color into OpenGL color format.
	 * @param color some color
	 * @return array of floats: [red, green, blue, alpha]
	 */
	public static float[] convertColor(@ColorInt int color) {
		return new float[] {
			Color.red(color) / 255f,
			Color.green(color) / 255f,
			Color.blue(color) / 255f,
			Color.alpha(color) / 255f
		};
	}

    public static float normalize(float val, float from, float to) {
        if (val < from)
            return 0;
        if (val > to)
            return 1;
        return val / (to - from);
    }

	public static float normalizeGl(float val, float newFromVal, float newToVal) {
		return normalizeGl(val, -1, 1, newFromVal, newToVal);
	}

	public static float normalizeGl(float val, float fromVal, float toVal, float newFromVal, float newToVal) {
		float perc = (val - fromVal) / (toVal - fromVal);
		return newFromVal + perc * (newToVal - newFromVal);
	}

	/**
	 * Convert square of magnitude to decibels
	 * @param squareMag square of magnitude
	 * @return decibels
	 */
	public static float magnitudeToDb(float squareMag) {
		if (squareMag == 0)
			return 0;
		return (float) (20 * Math.log10(squareMag));
	}

	/**
	 * Exponential smoothing (Holt - Winters).
	 * @param prevValue previous values in series <code>X[i-1]</code>
	 * @param newValue new value in series <code>X[i]</code>
	 * @param a smooth coefficient
	 * @return smoothed value
	 */
	public static float smooth(float prevValue, float newValue, float a) {
		return a * newValue + (1 - a) * prevValue;
	}

	/**
	 * Quadratic Bezier curve.
	 * @param t time
	 * @param p0 start point
	 * @param p1 control point
	 * @param p2 end point
	 * @return point on Bezier curve at some time <code>t</code>
	 */
	public static float quad(float t, float p0, float p1, float p2) {
		return (float) (p0 * Math.pow(1 - t, 2) + p1 * 2 * t * (1 - t) + p2 * t * t);
	}

	public static float randomize(float value, Random random) {
		float perc = between((random.nextInt(100) + 70) / 100, 0.7f, 1.3f);
		return perc * value;
	}

	/**
	 * Check if all elements are null
	 * @param array some array
	 * @return true if all elements are null, false otherwise
	 */
	public static <T> boolean allElementsAreNull(T[] array) {
		for (T element : array) {
			if (element != null)
				return false;
		}
		return true;
	}

    /**
     * Get index of object in array.
     * @param array some array
     * @param object some object
     * @return index of object in array or -1
     */
    public static <T> int indexOf(@NonNull T[] array, @Nullable T object) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == object)
                return i;
        }
        return -1;
    }

    /**
     * Check if all array elements are false
     * @param array some array
     * @return true if all elements are equals to false
     */
    public static boolean allElementsAreFalse(@NonNull boolean[] array) {
        for (boolean wavesWorkingState : array) {
            if (wavesWorkingState)
                return false;
        }
        return true;
    }

    /**
     * Check if all array elements equal to zero
     * @param array some array
     * @return true if all elements equal to zero
     */
    public static boolean allElementsAreZero(byte[] array) {
        for (byte b : array) {
            if (b != 0)
                return false;
        }
        return true;
    }
}

/**
 * Copyright 2013, Haruki Hasegawa
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */

/**
 * from http://stackoverflow.com/questions/2641111/where-is-android-os-systemproperties
 */

package com.cleveroad.audiovisualization.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class SystemPropertiesProxy {
  private static final String TAG = "SystemPropertiesProxy";

  /**
   * Get the value for the given key, returned as a boolean. Values 'n', 'no',
   * '0', 'false' or 'off' are considered false. Values 'y', 'yes', '1', 'true'
   * or 'on' are considered true. (case insensitive). If the key does not exist,
   * or has any other value, then the default result is returned.
   * 
   * @param key the key to lookup
   * @param def a default value to return
   * @return the key parsed as a boolean, or def if the key isn't found or is
   *         not able to be parsed as a boolean.
   * @throws IllegalArgumentException if the key exceeds 32 characters
   */
  public static Boolean getBoolean(Context context, String key, boolean def)
      throws IllegalArgumentException {
    return getBoolean(context.getClassLoader(), key, def);
  }

  public static Boolean getBoolean(ClassLoader cl, String key, boolean def)
      throws IllegalArgumentException {

    Boolean ret = def;

    try {
      @SuppressWarnings("rawtypes")
      Class SystemProperties = cl.loadClass("android.os.SystemProperties");

      // Parameters Types
      @SuppressWarnings("rawtypes")
      Class[] paramTypes = new Class[2];
      paramTypes[0] = String.class;
      paramTypes[1] = boolean.class;

      @SuppressWarnings("unchecked")
      Method getBoolean = SystemProperties.getMethod("getBoolean", paramTypes);

      // Parameters
      Object[] params = new Object[2];
      params[0] = new String(key);
      params[1] = Boolean.valueOf(def);

      ret = (Boolean) getBoolean.invoke(SystemProperties, params);

    } catch (IllegalArgumentException iAE) {
      throw iAE;
    } catch (Exception e) {
      Log.e(TAG, "getBoolean(context, key: " + key + ", def:" + def + ")", e);
      ret = def;
    }

    return ret;
  }
}

package com.cleveroad.example;


import android.os.Process;
import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allowed values for thread priority
 */
@IntDef(flag = true, value = {
        Process.THREAD_PRIORITY_BACKGROUND,
        Process.THREAD_PRIORITY_AUDIO,
        Process.THREAD_PRIORITY_DEFAULT,
        Process.THREAD_PRIORITY_DISPLAY,
        Process.THREAD_PRIORITY_URGENT_AUDIO,
        Process.THREAD_PRIORITY_URGENT_DISPLAY,
        Process.THREAD_PRIORITY_FOREGROUND,
        Process.THREAD_PRIORITY_LOWEST
})
@Retention(RetentionPolicy.SOURCE)
@Documented
@interface ProcessPriority {
}

package com.aliyun.sls.android.plugin.unity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author gordon
 * @date 2022/8/23
 */
@SuppressWarnings("SameParameterValue")
public final class Reflection {

    static Object getStaticField(
        String clazzName,
        String fileName,
        Object param
    ) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            Field field = clazz.getDeclaredField(fileName);
            field.setAccessible(true);
            return field.get(param);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    static Object invokeStaticMethod(
        String clazzName,
        String methodName,
        Object[] param1ArrayOfObject,
        Class<?>... params
    ) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            Method method = clazz.getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return method.invoke(null, param1ArrayOfObject);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    static Object newInstance(
        String clazzName,
        Object[] params,
        Class<?>... argsClazz
    ) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            if (params == null) { return clazz.newInstance(); }
            Constructor<?> constructor = clazz.getConstructor(argsClazz);
            return constructor.newInstance(params);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}

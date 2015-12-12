/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 baoyongzhang <baoyz94@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.baoyz.treasure;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by baoyz on 15/11/10.
 */
public class Treasure {

    public static final String PREFERENCES_SUFFIX = "$$Preferences";

    private static HashMap<Key, Object> sPreferencesCache;
    private static Converter.Factory sConverterFactory;

    static {
        sPreferencesCache = new HashMap();
        sConverterFactory = new SimpleConverterFactory();
    }

    public static void setConverterFactory(Converter.Factory factory) {
        sConverterFactory = factory;
    }

    public static <T> T get(Context context, Class<T> interfaceClass) {
        return get(context, interfaceClass, null);
    }

    public static <T> T get(Context context, Class<T> interfaceClass, String id) {
        Key key = new Key();
        key.interfaceClass = interfaceClass;
        key.id = id;
        T value = (T) sPreferencesCache.get(key);
        if (value != null) {
            return value;
        }

        value = findPreferences(context, interfaceClass, id);

        if (value != null) {
            sPreferencesCache.put(key, value);
            return value;
        }

        return null;
    }

    private static <T> T findPreferences(Context context, Class<T> interfaceClass, String id) {
        T value = null;

        try {
            value = (T) PreferencesFinder.get(context, interfaceClass, id, sConverterFactory);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError error) {
            // Not found PreferencesFinder class
            error.printStackTrace();
        }

        // If not found PreferencesFinder, that use reflect to find Preferences.

        try {
            final Constructor<?> constructor;
            if (id == null) {
                constructor = Class.forName(getPreferencesClassName(interfaceClass)).getConstructor(Context.class, Converter.Factory.class);
                value = (T) constructor.newInstance(context, sConverterFactory);
            } else {
                constructor = Class.forName(getPreferencesClassName(interfaceClass)).getConstructor(Context.class, Converter.Factory.class, String.class);
                value = (T) constructor.newInstance(context, sConverterFactory, id);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    private static String getPreferencesClassName(Class interfaceClass) {
        final String interfaceName = interfaceClass.getCanonicalName();
        return interfaceName + PREFERENCES_SUFFIX;
    }

    static class Key {
        Class<?> interfaceClass;
        String id;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (interfaceClass != null ? !interfaceClass.equals(key.interfaceClass) : key.interfaceClass != null)
                return false;
            return !(id != null ? !id.equals(key.id) : key.id != null);

        }

        @Override
        public int hashCode() {
            int result = interfaceClass != null ? interfaceClass.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }
    }
}

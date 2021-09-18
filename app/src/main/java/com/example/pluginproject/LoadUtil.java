package com.example.pluginproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class LoadUtil {

    private final static String apkPath = "/sdcard/plugin-debug.apk";

    public static void loadClass(Context context) {

        try {

            Class<?> clazz = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = clazz.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElements = dexPathListClass.getDeclaredField("dexElements");
            dexElements.setAccessible(true);

            //获取宿主的类加载器
            ClassLoader hostClassLoader = context.getClassLoader();
            Object hostPathList = pathListField.get(hostClassLoader);
            Object[] hostDexElements = (Object[]) dexElements.get(hostPathList);

            //获取插件的类加载器
            DexClassLoader pluginClassLoader = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(), null, hostClassLoader);
            Object pluginPathList = pathListField.get(pluginClassLoader);
            Object[] pluginDexElements = (Object[]) dexElements.get(pluginPathList);

            // 创建一个新数组
            Object[] newDexElements = (Object[]) Array.newInstance(hostDexElements.getClass().getComponentType(), hostDexElements.length + pluginDexElements.length);

            System.arraycopy(hostDexElements, 0, newDexElements, 0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0, newDexElements, hostDexElements.length, pluginDexElements.length);

            // 赋值
            dexElements.set(hostPathList, newDexElements);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Resources loadResources(Context context) {

        // assets.addAssetPath(key.mResDir)
        try {
            AssetManager assetManager = AssetManager.class.newInstance();

            // 让这个 AssetManager 对象加载的资源是插件的
            Method addAssetPathMethod = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, apkPath);

            // 如果传入的是 Activity 的 context  会不断循环  导致崩溃
            Resources resources = context.getResources();

            // 加载插件的资源的 Resource
            return new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

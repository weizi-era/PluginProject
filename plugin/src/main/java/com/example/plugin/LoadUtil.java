package com.example.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Method;

public class LoadUtil {

    private static Resources mResources;
    private final static String apkPath = "/sdcard/plugin-debug.apk";

    public static Resources getResources(Context context) {
        if (mResources == null) {
            mResources = loadResources(context);
        }
        return mResources;
    }

    private static Resources loadResources(Context context) {
        // assets.addAssetPath(key.mResDir)
        try {
            AssetManager assetManager = AssetManager.class.newInstance();

            // 让这个 AssetManager 对象加载的资源是插件的
            Method addAssetPathMethod = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, apkPath);

            // 如果传入的是 Activity 的 context  会不断循环  导致崩溃
            Resources resources = context.getResources();

            // 加载插件的资源
            return new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

package com.example.pluginproject;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class HookUtil {

    private static final String TARGET_INTENT = "target_intent";

    public static void hookAMS() {

        try {

            // IActivityTaskManagerSingleton 是静态的  可以通过反射获取 singleton 对象
            // 获取 singleton 对象
            Class<?> amClass = null;
            Field singletonField = null;

            // todo  版本适配
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {  // API 小于 26
                amClass = Class.forName("android.app.ActivityManagerNative");
                singletonField = amClass.getDeclaredField("gDefault");  // 26 <= API <= 28
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                amClass = Class.forName("android.app.ActivityManager");
                singletonField = amClass.getDeclaredField("IActivityManagerSingleton");
            } else {  // API > 28
                amClass = Class.forName("android.app.ActivityTaskManager");
                singletonField = amClass.getDeclaredField("IActivityTaskManagerSingleton");
            }
            singletonField.setAccessible(true);
            Object singleton = singletonField.get(null);

            // 因为 getService() 是静态方法，所以通过反射可以获取 getService() 的返回值 == IActivityTaskManager 对象
            // 获取系统的 IActivityTaskManager 对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            final Object mInstance;
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                mInstance = mInstanceField.get(singleton);
            } else {
                // Android 10 以后google 禁止反射非SDK api, 通过Singleton 的get 方法可获得IActivityTaskManager对象
                Method getMethod = singletonClass.getMethod("get");
                mInstance = getMethod.invoke(singleton);
            }


            Log.d("TAG", "hookAMS: " + mInstance);

            final Class<?> iActivityTaskManagerClass;

            // todo  版本适配
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                iActivityTaskManagerClass = Class.forName("android.app.IActivityManager");
            } else {
                iActivityTaskManagerClass = Class.forName("android.app.IActivityTaskManager");
            }

           // Class<?> iActivityTaskManagerClass = Class.forName("android.app.IActivityTaskManager");

            // 创建动态代理对象
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityTaskManagerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                    // 修改Intent
                    // 过滤
                    if ("startActivity".equals(method.getName())) {

                        int index = -1;
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] instanceof Intent) {
                                index = i;
                                break;
                            }
                        }

                        // 启动插件的
                        Intent intent = (Intent) args[index];
                        // 启动代理的
                        Intent proxyIntent = new Intent();
                        proxyIntent.setClassName("com.example.pluginproject",
                                "com.example.pluginproject.ProxyActivity");

                        // 保存插件的intent
                        proxyIntent.putExtra(TARGET_INTENT, intent);

                        args[index] = proxyIntent;
                    }

                    // 不改变原有的执行流程
                    return method.invoke(mInstance, args);
                }
            });

            // 把 ActivityTaskManager.getService() 替换成 proxyInstance
            mInstanceField.set(singleton, proxyInstance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void hookHandler() {

        try {
            // 获取ActivityThread 对象
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Field activityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            activityThreadField.setAccessible(true);
            Object activityThread = activityThreadField.get(null);

            // 获取 mH 对象
            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(activityThread);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            Handler.Callback callback = new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {

                    // 通过msg 可以拿到Intent 可以换回执行插件的Intent

                    // 找到Intent方便替换的地方  在 ActivityClientRecord 类中
                    // msg.obj == ActivityClientRecord
                    switch (msg.what) {
                        case 100:
                            try {
                                Field intentField = msg.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                // 启动代理intent
                                Intent proxyIntent = (Intent) intentField.get(msg.obj);
                                // 启动插件的intent
                                Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                if (intent != null) {
                                    intentField.set(msg.obj, intent);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 159:
                            try {
                                // 获取 mActivityCallbacks 对象
                                Field mActivityCallbacksField = msg.obj.getClass().getDeclaredField("mActivityCallbacks");
                                mActivityCallbacksField.setAccessible(true);
                                List mActivityCallbacks = (List) mActivityCallbacksField.get(msg.obj);
                                for (int i = 0; i < mActivityCallbacks.size(); i++) {
                                    if (mActivityCallbacks.get(i).getClass().getName()
                                            .equals("android.app.servertransaction.LaunchActivityItem")) {
                                        Object launchActivityItem = mActivityCallbacks.get(i);

                                        // 获取启动代理的Intent
                                        Field mIntentField = launchActivityItem.getClass().getDeclaredField("mIntent");
                                        mIntentField.setAccessible(true);
                                        Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);

                                        // 获取启动插件的Intent
                                        Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                        if (intent != null) {
                                            mIntentField.set(launchActivityItem, intent);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    return false;
                }
            };
            mCallbackField.set(mH, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

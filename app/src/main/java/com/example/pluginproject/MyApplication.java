package com.example.pluginproject;

import android.app.Application;
import android.content.res.Resources;

public class MyApplication extends Application {

    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        LoadUtil.loadClass(this);

        HookUtil.hookAMS();
      //  HookUtil.hookHandler();

     //   resources = LoadUtil.loadResources(this);
    }

//    @Override
//    public Resources getResources() {
//        return resources == null ? super.getResources() : resources;
//    }
}

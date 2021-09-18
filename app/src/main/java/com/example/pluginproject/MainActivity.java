package com.example.pluginproject;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.permissionx.guolindev.PermissionX;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, "需要您同意以下授权才能正常使用","同意","拒绝"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Toast.makeText(MainActivity.this, "您同意了所有权限", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "您拒绝了以下权限" + deniedList, Toast.LENGTH_SHORT).show();
                    }
                });

        findViewById(R.id.load_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //打印类加载器
                printClassLoader();


                try {
                    Class<?> clazz = Class.forName("com.example.plugin.Test");
                    Method print = clazz.getMethod("print");
                    print.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.plugin", "com.example.plugin.MainActivity"));
                startActivity(intent);

            }
        });

    }

    private void printClassLoader() {

        ClassLoader classLoader = getClassLoader();
        if (classLoader != null) {
            Log.e("TAG", "printClassLoader: " + classLoader);
        }

        Log.e("TAG", "printClassLoader: " + MainActivity.class.getClassLoader());
    }
}
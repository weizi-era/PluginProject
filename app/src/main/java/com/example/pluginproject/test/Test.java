package com.example.pluginproject.test;

import java.lang.reflect.Field;

public class Test {

    public static void main(String[] args) {

        try {

            Parent.getService().test();

            Class<?> parentClass = Class.forName("com.example.pluginproject.test.Parent");
            Field childField = parentClass.getDeclaredField("child");
            childField.setAccessible(true);
            Object child = childField.get(null);  // 这里拿到了 child 对象
            System.out.println(child);

            Class<?> childClass = Class.forName("com.example.pluginproject.test.Child");
            Field childInstanceField = childClass.getDeclaredField("childInstance");
            childInstanceField.setAccessible(true);
            Itest ITestObject = (Itest) childInstanceField.get(child);  // 这里拿到的是Itest对象？？
            System.out.println("ITestObject对象：" + ITestObject);


          //  childInstanceField.set(child, ITestObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.example.pluginproject.test;


public class Parent {


    public static Itest getService() {
        return child.get();
    }

    private static final Child<Itest> child = new Child<Itest>() {
        @Override
        protected Itest create() {
            return new Itest() {
                @Override
                public void test() {
                    System.out.println("test: 这是Itest接口对象");
                }
            };
        }
    };

}


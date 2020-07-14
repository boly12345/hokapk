package com.xd.hokapk2;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bdd
 */
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //获取当前类加载器
        ClassLoader classLoader = this.getClassLoader();

        //首先获取到pathList属性
        Field pathList = null;
        //根据双亲委托的机制来获取对应的属性
        for (Class<?> aClass = classLoader.getClass(); aClass != null; aClass = aClass.getSuperclass()) {
            try {
                //BaseDexClassLoader-->pathList
                pathList = aClass.getDeclaredField("pathList");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        //一般来说都是私有属性，判断后设为true
        if (!pathList.isAccessible()) {
            pathList.setAccessible(true);
        }

        //拿到pathList的val
        //o.getClass()=DexPathList.class
        Object o = null;
        try {
            o = pathList.get(classLoader);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        //获取DexPathList.class中dexElements属性
        Field dexElementsField = null;
        for (Class<?> aClass = o.getClass(); aClass != null; aClass = aClass.getSuperclass()) {
            try {
                //获取dexElements属性
                //DexPathList -->dexElements
                dexElementsField = aClass.getDeclaredField("dexElements");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        if (!dexElementsField.isAccessible()) {
            //设置访问权限
            dexElementsField.setAccessible(true);
        }

        //获取到程序中的dexElements(旧的apk中的dexs)
        Object[] dexElements = null;
        try {
            dexElements = (Object[]) dexElementsField.get(o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        //====================================分割线==============================================//
        //接下来获取补丁包中的文件

        //获取缓存目录中的a.dex(名字随意)
        String filePath = getCacheDir().getPath() + "/a.dex";
        //获取补丁包中的dexElements数组，在分析源码中
        //DexPathList.class中的makeDexElements()  是创建了一个dexElements数组
        Method makeDexElements = null;
        for (Class<?> aClass = o.getClass(); aClass != null; aClass = aClass.getSuperclass()) {
            try {
                makeDexElements = aClass.getDeclaredMethod("makeDexElements", List.class, File.class, List.class,ClassLoader.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        if (!makeDexElements.isAccessible()) {
            makeDexElements.setAccessible(true);
        }

        List<File> files = new ArrayList<>();
        File file = new File(filePath);
        if (file.exists()) {
            files.add(file);
        }
        List<IOException> exceptions = new ArrayList<>();
        //获取补丁包中的dexElements
        Object[] dexElements2 = null;
        try {
            dexElements2 = (Object[]) makeDexElements.invoke(o, files, null, exceptions,classLoader);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


        //====================================分割线==============================================//
        //接下来合并dex

        //创建一个新的数组
        Object[] newElements = (Object[]) Array.newInstance(dexElements.getClass().getComponentType(),
                dexElements.length + dexElements2.length);
        //将补丁包的dexs放在最前面
        System.arraycopy(dexElements2, 0, newElements, 0, dexElements2.length);
        //接着存放就apk中的dexs
        System.arraycopy(dexElements, 0, newElements, dexElements2.length, dexElements.length);

        //将合并后的数据赋值给dexElements
        try {
            dexElementsField.set(o,newElements);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

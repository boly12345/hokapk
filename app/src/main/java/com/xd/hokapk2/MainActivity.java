package com.xd.hokapk2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //定义一个bug
//        throw new NullPointerException("空异常！！！");
        //将程序运行在手机上
    }
}
package com.kingkung.lib;

import com.kingkung.train.TrainPage;

import java.util.Arrays;

public class MyClass {
    public static void main(String[] args) {
        TrainPage trainPage = new TrainPage();
        trainPage.onCreate();
//        trainPage.presenter.sendEmail(Arrays.asList("745440793@qq.com"), "抢票", " 有余票");
        try {
            Thread.sleep(1000 * 60 * 60 * 24 * 30L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

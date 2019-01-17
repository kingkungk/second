package com.kingkung.lib;

import com.kingkung.train.TrainPage;

public class MyClass {
    public static boolean isProxy = false;
    public static boolean isTestSendEmail = false;

    public static void main(String[] args) {
        initArg(args);

        TrainPage trainPage = new TrainPage();
        trainPage.onCreate();

        sleep();
    }

    public static void sleep() {
        try {
            Thread.sleep(1000 * 60 * 60 * 24 * 30L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void initArg(String[] args) {
        if (args.length == 2) {
            String key = args[0];
            String value = args[1];
            if (key.startsWith("proxy")) {
                isProxy = Boolean.parseBoolean(value);
            } else if (key.startsWith("email")) {
                isTestSendEmail = Boolean.parseBoolean(value);
            }
        } else if (args.length == 4 && args[0].startsWith("proxy")
                && args[2].startsWith("email")) {
            isProxy = Boolean.parseBoolean(args[1]);
            isTestSendEmail = Boolean.parseBoolean(args[3]);
        }
    }
}

package com.kingkung.train.utils;

import java.io.File;

public class FileUtil {
    public static File trainDataDir = new File("F:" + File.separator + "train-data");
    public static File cookieFile = new File(trainDataDir, "cookies.txt");
    public static File logDir = new File(trainDataDir, "logs");

    static {
        if (!trainDataDir.exists()) {
            trainDataDir.mkdirs();
        }
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }
}

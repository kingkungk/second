package com.kingkung.train.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

    private static Logger logger;

    public Log() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.ENGLISH);
            logger = Logger.getLogger("train");
            removeParentHandler(logger);
            logger.setLevel(Level.ALL);
            FileHandler fileHandler = new FileHandler(FileUtil.logDir.getAbsolutePath() +
                    File.separator + format.format(new Date()) + ".log");
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initLog() {
        if (logger == null) {
            synchronized (Log.class) {
                if (logger == null) {
                    new Log();
                }
            }
        }
    }

    public void removeParentHandler(Logger logger) {
        Logger parentLogger = logger.getParent();
        while (parentLogger != null) {
            Handler[] handlers = parentLogger.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                parentLogger.removeHandler(handlers[i]);
            }
            parentLogger = parentLogger.getParent();
        }
    }

    public static void print(String message) {
        initLog();
        logger.info(message);
    }

    class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return "[" + new Date().toString() + "]" + "[" + record.getLevel() + "]" + record.getClass() + record.getMessage() + "\n";
        }
    }
}

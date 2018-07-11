/**
 * Logger.java <br>
 * Copyright 2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.utils;

import android.os.Environment;
import android.util.Log;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.IOException;

import de.mindpipe.android.logging.log4j.LogCatAppender;

/**
 * Logger facade. Can be used to log to logcat alone or to a log file as well as logcat
 *
 * @author Avaya Inc
 */
public class Logger {

    public enum LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }

    private static final String FOLDER = Environment.getExternalStorageDirectory().toString();

    private static final long MAXIMUM_ALLOWED_FILE_SIZE = 512; // Don't allow larger even if user enters a higher value
    private static final int MAXIMUM_ALLOWED_BACK_UP_FILES_SIZE = 5; // Don't allow more even if user enters a higher value

    private static boolean sUseLog4j = true;

    private String mTag = "";

    private org.apache.log4j.Logger mLog4jLogger;
    private static Level mLog4jLogLevel = Level.ALL;

    private static final String FILE_PATTERN = "%d - [%p::%c] - %m%n";
    private static final String LOG_CAT_PATTERN = "%m%n";
    private static final boolean IMMEDIATE_FLUSH = true;
    private static final boolean USE_LOG_CAT_APPENDER = true;
    private static final boolean USE_FILE_APPENDER = true;
    private static final boolean RESET_CONFIGURATION = true;
    private static final boolean INTERNAL_DEBUGGING = false;

    private static String sFileName = "";
    private static int sMaxBackupSize = MAXIMUM_ALLOWED_BACK_UP_FILES_SIZE;
    private static long sMaxFileSize = MAXIMUM_ALLOWED_FILE_SIZE * 1024;

    public static Logger getLogger(String tag) {
        return new Logger(tag);
    }

    public static void configure(boolean logToDisk, String strLogLevel, String logFileName, String maxFileSize, String maxBackUps) {

        sUseLog4j = logToDisk;

        Log.d(Logger.class.getSimpleName(), "Configure log settings, log to file: " + logToDisk + ", log level: " + strLogLevel);

        LogLevel logLevel = getLogLevel(strLogLevel);

        switch (logLevel) {
            case VERBOSE:
                mLog4jLogLevel = Level.ALL;
                break;
            case DEBUG:
                mLog4jLogLevel = Level.DEBUG;
                break;
            case INFO:
                mLog4jLogLevel = Level.INFO;
                break;
            case WARN:
                mLog4jLogLevel = Level.WARN;
                break;
            case ERROR:
                mLog4jLogLevel = Level.ERROR;
                break;
            default:
                mLog4jLogLevel = Level.ALL;
        }

        if (logFileName == null) {
            logFileName = "";
        }

        sFileName = FOLDER + File.separator + logFileName;

        setMaxFileSize(maxFileSize);
        setMaxBackUps(maxBackUps);


        Log.d(Logger.class.getSimpleName(), "Logger configure summary: log to file: " + sUseLog4j + ", log level: " + mLog4jLogLevel + ", file name: " + sFileName + ", max file size: " + sMaxFileSize + ", max number of back-ups: " + sMaxBackupSize);
    }

    private static LogLevel getLogLevel(String strLogLevel) {
        LogLevel logLevel = LogLevel.VERBOSE;

        if (strLogLevel != null) {
            strLogLevel = strLogLevel.trim().toLowerCase();

            if (strLogLevel.equals(Constants.LOG_LEVEL_DEBUG)) {
                logLevel = LogLevel.DEBUG;
            } else if (strLogLevel.equals(Constants.LOG_LEVEL_INFO)) {
                logLevel = LogLevel.INFO;
            } else if (strLogLevel.equals(Constants.LOG_LEVEL_WARN)) {
                logLevel = LogLevel.WARN;
            } else if (strLogLevel.equals(Constants.LOG_LEVEL_ERROR)) {
                logLevel = LogLevel.ERROR;
            }
        }

        return logLevel;
    }

    private static void setMaxFileSize(String strMaxFileSize) {
        try{
            if (strMaxFileSize != null) {
                long maxFileSize = Long.parseLong(strMaxFileSize);

                if (maxFileSize > 0 && maxFileSize <= MAXIMUM_ALLOWED_FILE_SIZE) {
                    sMaxFileSize = maxFileSize * 1024;
                }
            }
        } catch (Exception e) {
            Log.e(Logger.class.getSimpleName(), "Exception", e);
        }
    }

    private static void setMaxBackUps(String strMaxBackUps) {
        try{
            if (strMaxBackUps != null) {
                int maxBackUps = Integer.parseInt(strMaxBackUps);

                if (maxBackUps > 0 && maxBackUps <= MAXIMUM_ALLOWED_BACK_UP_FILES_SIZE) {
                    sMaxBackupSize = maxBackUps;
                } else {
                    sMaxBackupSize = MAXIMUM_ALLOWED_BACK_UP_FILES_SIZE;
                }
            }
        } catch (Exception e) {
            Log.e(Logger.class.getSimpleName(), "Exception", e);
        }
    }

    private Logger(String tag) {
        configure(tag);
    }

    public void v(String message) {
        try {
            if (sUseLog4j) {
                mLog4jLogger.trace(message);
            } else {
                Log.v(mTag, message);
            }
        } catch (Exception e) {
        }
    }

    public void d(String message) {
        try {
            if (sUseLog4j) {
                mLog4jLogger.debug(message);
            } else {
                Log.d(mTag, message);
            }
        } catch (Exception e) {
        }
    }

    public void i(String message) {
        try {
            if (sUseLog4j) {
                mLog4jLogger.info(message);
            } else {
                Log.i(mTag, message);
            }
        } catch (Exception e) {
        }

    }

    public void w(String message) {
        try {
            if (sUseLog4j) {
                mLog4jLogger.warn(message);
            } else {
                Log.w(mTag, message);
            }
        } catch (Exception e) {
        }
    }

    public void w(String message, Throwable tr) {
        try {
            if (sUseLog4j) {
                mLog4jLogger.warn(message, tr);
            } else {
                Log.w(mTag, message, tr);
            }
        } catch (Exception e) {
        }
    }

    public void e(String message) {
        try {
            if (sUseLog4j) {
                mLog4jLogger.error(message);
            } else {
                Log.e(mTag, message);
            }
        } catch (Exception e) {
        }
    }

    public void e(String message, Throwable tr) {
        try {
            if (sUseLog4j) {
                mLog4jLogger.error(message, tr);
            } else {
                Log.e(mTag, message, tr);
            }
        } catch (Exception e) {
        }
    }

    private void configure(String tag) {
        try {
            mTag = tag;
            if (sUseLog4j) {
                mLog4jLogger = org.apache.log4j.Logger.getLogger(tag);

                if (RESET_CONFIGURATION) {
                    LogManager.getLoggerRepository().resetConfiguration();
                }

                LogLog.setInternalDebugging(INTERNAL_DEBUGGING);

                if (USE_FILE_APPENDER) {
                    configureFileAppender();
                }

                if (USE_LOG_CAT_APPENDER) {
                    configureLogCatAppender();
                }

                mLog4jLogger.setLevel(mLog4jLogLevel);
            }
        } catch (Exception e) {
        }
    }

    private void configureFileAppender() {
        try {
            final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
            final RollingFileAppender rollingFileAppender;
            final Layout fileLayout = new PatternLayout(FILE_PATTERN);

            try {
                rollingFileAppender = new RollingFileAppender(fileLayout, sFileName);
            } catch (final IOException e) {
                throw new RuntimeException("Exception configuring log system", e);
            }

            rollingFileAppender.setMaxBackupIndex(sMaxBackupSize);
            rollingFileAppender.setMaximumFileSize(sMaxFileSize);
            rollingFileAppender.setImmediateFlush(IMMEDIATE_FLUSH);

            logger.addAppender(rollingFileAppender);
        } catch (Exception e) {
        }
    }

    private void configureLogCatAppender() {
        try {
            final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();

            final Layout logCatLayout = new PatternLayout(LOG_CAT_PATTERN);
            final LogCatAppender logCatAppender = new LogCatAppender(logCatLayout);

            logger.addAppender(logCatAppender);
        } catch (Exception e) {
        }
    }
}

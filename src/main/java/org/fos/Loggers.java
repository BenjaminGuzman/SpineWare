package org.fos;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Loggers {
    public static final String ERROR_LOGGER_NAME = "Error logger";
    public static final String DEBUG_LOGGER_NAME = "Debug logger";

    // logger used to log errors, such as exceptions, the logger will also write the output to a file
    public static final Logger errorLogger = Logger.getLogger(Loggers.ERROR_LOGGER_NAME);

    // logger used to log messages for debugging purposes, the logger will write to STDOUT
    public static final Logger debugLogger = Logger.getLogger(Loggers.DEBUG_LOGGER_NAME);

    /**
     * Initiates loggers for the app
     */
    public static void init() {
        try {
            // get the OS tmp directory
            File tmpErrorFile = File.createTempFile("SpineWare", ".error.log");

            // create a file handler from that tmp file
            FileHandler errorLogsFileHandler = new FileHandler(tmpErrorFile.getCanonicalPath());

            Loggers.errorLogger.addHandler(errorLogsFileHandler);
        } catch (IOException e) {
            System.err.println("Error while creating the temp file to log errors");
            e.printStackTrace();
        }
    }
}

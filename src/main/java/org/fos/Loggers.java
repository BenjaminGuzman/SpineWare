/*
 * Copyright © 2020 Benjamín Guzmán
 * Author: Benjamín Guzmán <bg@benjaminguzman.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

package it.polimi.ingsw.client;

import it.polimi.ingsw.utilities.FilesUtil;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Contains some methods to print on the CLI and write a file
 */
public class ClientLogger {

    private static final String ANSI_CLS = "\u001b[2J";
    private static final String ANSI_HOME = "\u001b[H";
    private static FileHandler fh;

    /**
     * Private constructor because this class contains static methods only
     */
    private ClientLogger(){}

    /**
     * Prints something on the CLI without a new line at the end
     * @param message the message to print
     */
    public static synchronized void print(String message){
        System.out.print(message);
        System.out.flush();
    }

    /**
     * Clears and prints something on the CLI without a new line at the end
     * @param message the message to print
     */
    public static synchronized void printWithClear(String message){
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.print(message);
        System.out.flush();
    }

    /**
     * Prints something on the CLI with a new line at the end
     * @param message the message to print
     */
    public static synchronized void println(String message){
        System.out.println(message);
        System.out.flush();
    }

    /**
     * Clears and prints something on the CLI with a new line at the end
     * @param message the message to print
     */
    public static synchronized void printlnWithClear(String message){
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.println(message);
        System.out.flush();
    }

    /**
     * Initialises a logger so it can be used to write into a file
     * @param logger the logger to be initialised
     */
    public static void initLogger(Logger logger){
        logger.setUseParentHandlers(false);
        logger.addHandler(fh);
    }

    /**
     * Gets the references to the file where the logs will be written
     * @param name the name of file where the logs will be written
     */
    static synchronized void logToFile(String name) {
        try {
            File file = FilesUtil.fileToWrite(FilesUtil.LOG_FOLDER, name);
            fh = new FileHandler(file.getAbsolutePath());
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

}

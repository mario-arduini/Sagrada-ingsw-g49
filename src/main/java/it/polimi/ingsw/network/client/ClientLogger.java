package it.polimi.ingsw.network.client;

import it.polimi.ingsw.utilities.FilesUtil;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class ClientLogger {

    private static final String ANSI_CLS = "\u001b[2J";
    private static final String ANSI_HOME = "\u001b[H";
    private static FileHandler fh;

    static synchronized void print(Object object){
        System.out.print(object);
        System.out.flush();
    }

    static synchronized void printWithClear(Object object){
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.print(object);
        System.out.flush();
    }

    static synchronized void println(Object object){
        System.out.println(object);
        System.out.flush();
    }

    static synchronized void printlnWithClear(Object object){
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.println(object);
        System.out.flush();
    }

    static void initLogger(Logger logger){
        logger.setUseParentHandlers(false);
        logger.addHandler(fh);
    }

    static synchronized void LogToFile() {
        try {
            Optional<File> file = FilesUtil.listFiles(FilesUtil.LOG_FOLDER).stream().filter(logFile -> logFile.getName().equalsIgnoreCase("clientLog.log")).findFirst();

            if(file.isPresent()) {
                fh = new FileHandler(file.get().getAbsolutePath());
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            }
            else
                throw new IOException();
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

}

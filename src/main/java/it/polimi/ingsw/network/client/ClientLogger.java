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

    private ClientLogger(){}

    static synchronized void print(String message){
        System.out.print(message);
        System.out.flush();
    }

    static synchronized void printWithClear(String message){
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.print(message);
        System.out.flush();
    }

    static synchronized void println(String message){
        System.out.println(message);
        System.out.flush();
    }

    static synchronized void printlnWithClear(String message){
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.println(message);
        System.out.flush();
    }

    static void initLogger(Logger logger){
        logger.setUseParentHandlers(false);
        logger.addHandler(fh);
    }

    static synchronized void LogToFile() {
        try {
            Optional<File> file = FilesUtil.filesToWrite(FilesUtil.LOG_FOLDER, "clientLog.log").stream().filter(logFile -> logFile.getName().equalsIgnoreCase("clientLog.log")).findFirst();

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

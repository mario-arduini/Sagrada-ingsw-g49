package it.polimi.ingsw.utilities;

import it.polimi.ingsw.network.server.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Contains some methods to get a single file or to list files and folders from a directory
 */
public class FilesUtil {

    /**
     * Private constructor because this class contains static methods only
     */
    private FilesUtil(){
    }

    public static final String SCHEMA_FOLDER = "schema";
    public static final String TOOL_CARD_FOLDER = "toolcard";
    public static final String LOG_FOLDER = "log";

    /**
     * Creates a list of all the files with a same radix under a directory
     * @param radixName relative path with the radix the files have to have to be considered
     * @param number the number of files with the given radix
     * @return the list with the files from the directory
     */
    public static List<BufferedReader> listFiles(String radixName, int number) {

        List<BufferedReader> files = new ArrayList<>();
        String s;
        for(int i = 1; i <= number; i++) {
            s = radixName + i + ".json";
            files.add(new BufferedReader(new InputStreamReader(FilesUtil.class.getClassLoader().getResourceAsStream(s))));
        }
        return files;
    }

    /**
     * Gets a file from a given directory
     * @param dir the directory where the file required is in
     * @param fileName the name of the file required
     * @return the file required
     */
    public static File fileToWrite(String dir, String fileName) {

        try {
            Files.createDirectory(Paths.get(new File(dir).getAbsolutePath()));
            Files.createFile(Paths.get(new File(dir + "/" + fileName).getAbsolutePath()));
        } catch (IOException e) {
        }
         return new File(new File(dir + "/" + fileName).getAbsolutePath());
    }


    /**
     * Reads an integer value from a certain file
     * @param filename the name of the from where to read
     * @return the integer value read
     */
    public static int readIntFromFile(String filename){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(FilesUtil.class.getClassLoader().getResourceAsStream(filename)));
            String text;

            if ((text = reader.readLine()) != null)
                return Integer.parseInt(text);

        } catch (IOException e) {
            Logger.print("Read Int From File: parsing " + e.getMessage());
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Logger.print("Read Int From File: closing " + e.getMessage());
            }
        }
        return -1;
    }
}
package it.polimi.ingsw.utilities;

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
     * Creates a list of all the files with a same radix inside the jar
     * @param radixName relative path with the radix the files have to have to be considered
     * @param number the number of files with the given radix
     * @return the list with the files loaded
     */
    public static List<BufferedReader> listFilesInsideJar(String radixName, int number) {

        List<BufferedReader> files = new ArrayList<>();
        String path;
        for(int i = 1; i <= number; i++) {
            path = radixName + i + ".json";
            files.add(new BufferedReader(new InputStreamReader(FilesUtil.class.getClassLoader().getResourceAsStream(path))));
        }
        return files;
    }

    /**
     * Creates a list of all the files with a same radix under a directory outside the jar
     * @param radixName relative path with the radix the files have to have to be considered
     * @param number the number of files with the given radix
     * @return the list with the files from the directory
     */
    public static List<BufferedReader> listFilesOutsideJar(String radixName, int number, String folder) {

        List<BufferedReader> files = new ArrayList<>();
        String path;
        for(int i = 1; i <= number; i++) {
            path = radixName + i + ".json";
            try {
                files.add(new BufferedReader(new FileReader(new File(folder).getAbsolutePath() + "/" + path)));
            } catch (FileNotFoundException e) {
            }
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
}
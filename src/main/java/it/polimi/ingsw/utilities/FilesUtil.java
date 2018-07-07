package it.polimi.ingsw.utilities;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Contains some methods to list files and folders from a directory
 */
public class FilesUtil {
    /**
     * Returns a list of all the files under a directory
     *
     * @param directoryName to be listed
     */
    public static final String SCHEMA_FOLDER = "schema";
    public static final String TOOLCARD_FOLDER = "toolcard";
    public static final String LOG_FOLDER = "log";
    //public static final String LANGUAGES_FOLDER = "language";

    public static List<BufferedReader> listFiles(String radixName, int number) {

        List<BufferedReader> files = new ArrayList<>();
        String s;
        for(int i = 1; i <= number; i++) {
            if(i == 7 && radixName.equals(SCHEMA_FOLDER))
                i = 13;
            s = radixName + i + ".json";
            files.add(new BufferedReader(new InputStreamReader(FilesUtil.class.getClassLoader().getResourceAsStream(s))));
        }
        return files;
    }

    public static List<File> filesToWrite(String dir, String fileName) {

        try {
            Files.createDirectory(Paths.get(new File(dir).getAbsolutePath()));
            Files.createFile(Paths.get(new File(dir + "/" + fileName).getAbsolutePath()));
        } catch (IOException e) {
        }
        File directory = new File(new File(dir).getAbsolutePath());
        System.out.println(directory);
        //get all the files from a directory
        File[] fList = directory.listFiles();
        List<File> fileList = new ArrayList<>();

        for (File file : fList) {
            if (file.isFile()) {
                fileList.add(file);
            }
        }
        return fileList;
    }
}
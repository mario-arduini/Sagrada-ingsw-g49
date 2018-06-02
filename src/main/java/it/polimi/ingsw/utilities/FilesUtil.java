package it.polimi.ingsw.utilities;

import it.polimi.ingsw.model.Game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains some methods to list files and folders from a directory
 */
public class FilesUtil {
    /**
     * Returns a list of all the files under a directory
     *
     * @param directoryName to be listed
     */
    public final static String SCHEMA_FOLDER = "schemas";
    public final static String TOOLCARD_FOLDER = "toolcards";

    public static List<File> listFiles(String directoryName) {
        File directory = new File(FilesUtil.class.getClassLoader().getResource(directoryName).getPath());
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
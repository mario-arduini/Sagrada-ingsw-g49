package it.polimi.ingsw.utilities;

import it.polimi.ingsw.model.Game;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public static final String SCHEMA_FOLDER = "schemas";
    public static final String TOOLCARD_FOLDER = "toolcards";
    public static final String LOG_FOLDER = "log";
    public static final String LANGUAGES_FOLDER = "languages";

    public static List<File> listFiles(String directoryName) {

        InputStream inputStream = FilesUtil.class.getClassLoader().getResourceAsStream(directoryName);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        System.out.println(new File(".").getAbsolutePath());


        File directory = new File(FilesUtil.class.getClassLoader().getResource(directoryName).getPath());
        //get all the files from a directory
        File[] fList = directory.listFiles();
        List<File> fileList = new ArrayList<>();

        if (fList == null) return fileList;

        for (File file : fList) {
            if (file.isFile()) {
                fileList.add(file);
            }
        }
        return fileList;
    }
}
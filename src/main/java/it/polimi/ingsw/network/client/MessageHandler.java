package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.utilities.FilesUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

public class MessageHandler {
    private static HashMap<String,String> messages;

    private MessageHandler(){}

    public static void init(String language){
        if(messages==null){
            List<File> languages = FilesUtil.listFiles(FilesUtil.LANGUAGES_FOLDER);
            File chosenLanguage = null;
            for(File file:languages){
                if(file.getName().equals(language+".json")||
                        (chosenLanguage==null&&file.getName().equals("english.json"))) chosenLanguage = file;
            }

            JsonParser parser = new JsonParser();
            JsonObject languageJson = null;
            try {
                languageJson = parser.parse(new FileReader(chosenLanguage)).getAsJsonObject();
            } catch (FileNotFoundException e) {

            }
            Gson gson = new Gson();
            messages = gson.fromJson(languageJson,new TypeToken<HashMap<String,String>>(){}.getType());
        }
    }

    public static String get(String from){
        init("english");
        return messages.get(from);
    }
}

package it.polimi.ingsw.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.utilities.FilesUtil;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;

/**
 * A MessageHandler object retrieves messages from a json file.
 */
public class MessageHandler {
    private static HashMap<String,String> messages;

    private MessageHandler(){}

    /**
     * Initiates the MessageHandler in order to get prompts from a language.
     * @param language the name of the file to read.
     */
    private static void init(String language){
        if(messages == null){
            List<BufferedReader> languages = FilesUtil.listFilesInsideJar(language, 1);
            BufferedReader chosenLanguage = null;
            for(BufferedReader file:languages){
                chosenLanguage = file;
            }

            JsonParser parser = new JsonParser();
            JsonObject languageJson = parser.parse(chosenLanguage).getAsJsonObject();

            Gson gson = new Gson();
            messages = gson.fromJson(languageJson,new TypeToken<HashMap<String,String>>(){}.getType());
        }
    }

    /**
     * Gets a message from a json config file.
     * @param from the key of the json object
     * @return the value associated to the key.
     */
    public static String get(String from){
        init("english");
        return messages.get(from);
    }
}

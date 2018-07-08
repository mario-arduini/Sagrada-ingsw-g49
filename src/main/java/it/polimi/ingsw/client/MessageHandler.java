package it.polimi.ingsw.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.utilities.FilesUtil;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;

public class MessageHandler {
    private static HashMap<String,String> messages;

    private MessageHandler(){}

    private static void init(String language){
        if(messages == null){
            List<BufferedReader> languages = FilesUtil.listFiles(language, 1);
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

    public static String get(String from){
        init("english");
        return messages.get(from);
    }
}

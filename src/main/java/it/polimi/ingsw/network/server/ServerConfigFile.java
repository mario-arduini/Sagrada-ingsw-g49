package it.polimi.ingsw.network.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.utilities.FilesUtil;

import java.io.BufferedReader;
import java.util.List;

public class ServerConfigFile {
    private static final String CONFIG_FILE_NAME = "server_config";
    private static int secondsTimerSchema;
    private static int secondsTimerStartGame;
    private static int secondsTimerTurn;

    private ServerConfigFile() {
    }

    static void intiConfigParameters(){
        List<BufferedReader> files = FilesUtil.listFiles(CONFIG_FILE_NAME, 1);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(files.get(0)).getAsJsonObject();

        secondsTimerSchema = jsonObject.get("timer_schema").getAsInt();
        secondsTimerStartGame = jsonObject.get("timer_start_game").getAsInt();
        secondsTimerTurn = jsonObject.get("timer_schema").getAsInt();
    }

    public static int getSecondsTimerSchema() {
        return secondsTimerSchema;
    }

    public static int getSecondsTimerStartGame() {
        return secondsTimerStartGame;
    }

    public static int getSecondsTimerTurn() {
        return secondsTimerTurn;
    }
}

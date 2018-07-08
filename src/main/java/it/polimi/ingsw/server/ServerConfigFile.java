package it.polimi.ingsw.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.utilities.FilesUtil;

import java.io.BufferedReader;
import java.util.List;

/**
 * Contains methods to loads some server parameters from file and to get them
 */
public class ServerConfigFile {
    private static final String CONFIG_FILE_NAME = "server_config";
    private static int secondsTimerSchema;
    private static int secondsTimerStartGame;
    private static int secondsTimerTurn;
    private static int rmiPort;

    /**
     * Private constructor because the class contains static methods only
     */
    private ServerConfigFile() {
    }

    /**
     * Loads from file some parameters for the game and the server.
     */
    static void intiConfigParameters(){
        List<BufferedReader> files = FilesUtil.listFilesOutsideJar(CONFIG_FILE_NAME, 1, "");
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(files.get(0)).getAsJsonObject();

        secondsTimerSchema = jsonObject.get("timer_schema").getAsInt();
        secondsTimerStartGame = jsonObject.get("timer_start_game").getAsInt();
        secondsTimerTurn = jsonObject.get("timer_schema").getAsInt();
        rmiPort = jsonObject.get("rmi-port").getAsInt();
    }

    /**
     * Used to get the timer used during the schema choice
     * @return the timer used during the schema choice
     */
    public static int getSecondsTimerSchema() {
        return secondsTimerSchema;
    }

    /**
     * Used to get the timer used to start a game
     * @return the timer used to start a game
     */
    public static int getSecondsTimerStartGame() {
        return secondsTimerStartGame;
    }

    /**
     * Used to get the timer used during a turn of the game
     * @return the timer used during a turn of the game
     */
    public static int getSecondsTimerTurn() {
        return secondsTimerTurn;
    }

    /**
     * Used to get the port to locate the rmi registry.
     * @return the port where to locate the rmi registry.
     */
    static int getRmiPort(){
        return rmiPort;
    }

}

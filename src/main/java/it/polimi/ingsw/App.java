package it.polimi.ingsw;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.network.client.Client;

import java.util.Random;

public class App
{
    public static void main(String[] args) {

        Gson gson = new Gson();
        Color colors[][] = new Color[4][5];
        Color chose = Color.RED;
        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                colors[i][j] = chose;
                System.out.print(colors[i][j].toString() + " ");
            }
            System.out.println();
        }

        System.out.println();
        System.out.println(gson.toJson(colors));

        System.out.println();

        colors = gson.fromJson(gson.toJson(colors), Color[][].class);

        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(colors[i][j].toString() + " ");
            }
            System.out.println();
        }


        //create Gson object to send via socket

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("command", "schema");
        jsonObject.addProperty("schema1", gson.toJson(colors));

        System.out.println(jsonObject);
        System.out.println(jsonObject.get("command").getAsString());
        System.out.println(gson.fromJson(jsonObject.get("schema1").getAsString(), Color[][].class));

        colors = gson.fromJson(gson.toJson(colors), Color[][].class);

        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(colors[i][j].toString() + " ");
            }
            System.out.println();
        }



        //working on something received from socket as string

        String command = jsonObject.toString();
        JsonParser parser = new JsonParser();
        jsonObject = parser.parse(command).getAsJsonObject();

        System.out.println(jsonObject);
        System.out.println(jsonObject.get("command").getAsString());
        System.out.println(gson.fromJson(jsonObject.get("schema1").getAsString(), Color[][].class));

        colors = gson.fromJson(gson.toJson(colors), Color[][].class);

        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(colors[i][j].toString() + " ");
            }
            System.out.println();
        }

        command = null;
        jsonObject = parser.parse(command).getAsJsonObject();
    }
}


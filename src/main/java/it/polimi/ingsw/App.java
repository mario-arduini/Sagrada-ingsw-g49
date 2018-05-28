package it.polimi.ingsw;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.network.client.Client;

import java.util.Random;

public class App
{
    public static void main(String[] args) {

        Gson gson = new Gson();

        String name = "Luz Celestial";
        int difficulty = 3;
        Constraint[][] constraints = new Constraint[4][5];

        try {
            //constraints[0][0] = new Constraint(3);
            //constraints[0][1] = new Constraint(4);
            constraints[0][2] = new Constraint(Color.RED);
            constraints[0][3] = new Constraint(5);
            //constraints[0][4] = new Constraint(6);
            constraints[1][0] = new Constraint(Color.PURPLE);
            constraints[1][1] = new Constraint(4);
            //constraints[1][2] = new Constraint(5);
            constraints[1][3] = new Constraint(Color.GREEN);
            constraints[1][4] = new Constraint(3);
            constraints[2][0] = new Constraint(6);
            //constraints[2][1] = new Constraint(Color.RED);
            //constraints[2][2] = new Constraint(3);
            constraints[2][3] = new Constraint(Color.BLUE);
            //constraints[2][4] = new Constraint(1);
            //constraints[3][0] = new Constraint(1);
            constraints[3][1] = new Constraint(Color.YELLOW);
            constraints[3][2] = new Constraint(2);
            //constraints[3][3] = new Constraint(Color.RED);
            //constraints[3][4] = new Constraint(4);


            Schema schema = new Schema(difficulty, constraints, name);
            System.out.println(gson.toJson(schema));
        }catch (Exception e){

        }
        /*
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
        jsonObject = parser.parse(command).getAsJsonObject();*/
    }
}


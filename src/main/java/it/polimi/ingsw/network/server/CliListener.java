package it.polimi.ingsw.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CliListener implements Runnable{
    private Server server;
    private BufferedReader input;

    CliListener(Server server){
        this.server = server;
        this.input = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run(){
        String command;
        System.out.println("Type 'shutdown' to kill the server.");
        do {
            try {
                command = input.readLine();
            } catch (IOException e) {
                command = "ok";
            }
        }
        while(!command.equalsIgnoreCase("shutdown"));
        server.stop();
    }
}

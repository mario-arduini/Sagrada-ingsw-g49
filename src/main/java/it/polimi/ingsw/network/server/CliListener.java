package it.polimi.ingsw.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Command Line Interface Listener is a class used to shutdown the server when needed.
 * Run as a separate thread waits for a "shutdown" to be typed to stop the server.
 */
public class CliListener implements Runnable{
    private Server server;
    private BufferedReader input;

    /**
     * Creates a Command Line Listener.
     * @param server the server to shutdown on request.
     */
    CliListener(Server server){
        this.server = server;
        this.input = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Method that handles the order of instructions. To be called when a thread is created.
     */
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

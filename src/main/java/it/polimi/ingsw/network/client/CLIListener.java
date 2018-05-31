package it.polimi.ingsw.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLIListener implements Runnable{

    private boolean read;
    private BufferedReader input;
    private Client client;

    CLIListener(Client client){
        read = true;
        input = new BufferedReader(new InputStreamReader(System.in));
        this.client = client;
    }

    @Override
    public void run() {

        String command;

        while (read) {
            command = readString();
            if (command.equals("logout") || command.equals("exit"))
                client.logout();
            else {
                if (client.getWaitingInput()) {
                    client.setInput(command);
                }
            }
        }
    }

    void setRead(boolean read){
        this.read = read;
    }

    private String readString(){

        try {
            return input.readLine();
        } catch (IOException e) {
        }
        return readString();
    }
}

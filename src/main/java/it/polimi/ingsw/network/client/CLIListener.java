package it.polimi.ingsw.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLIListener implements Runnable {

    private CLIHandler cliHandler;
    private BufferedReader input;
    private boolean listen;

    CLIListener(CLIHandler cliHandler){
        this.cliHandler = cliHandler;
        input = new BufferedReader(new InputStreamReader(System.in));
        listen = true;
    }

    @Override
    public void run() {
        String choice;
        while (listen)
            try {
                choice = input.readLine();
                if(choice != null)
                    cliHandler.wakeUpInput(choice);
            } catch (IOException e) {
            }
    }

    void stopListening(){
        listen = false;
    }
}

package it.polimi.ingsw.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 * This class always listens what the user inserts into the CLI and notifies what has been inserted
 */
public class CLIListener implements Runnable {

    private CLIHandler cliHandler;
    private BufferedReader input;
    private boolean listen;

    /***
     * Creates an object of this class and initialises an object to read the CLI
     * @param cliHandler the object to be notified when something is inserted into the CLI
     */
    CLIListener(CLIHandler cliHandler){
        this.cliHandler = cliHandler;
        input = new BufferedReader(new InputStreamReader(System.in));
        listen = true;
    }

    /***
     * Begins to continuously listen what is inserted into the CLI on a different thread
     */
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

    /***
     * Stops this objects from listening the CLI
     */
    void stopListening(){
        listen = false;
    }
}

package it.polimi.ingsw.client;

import it.polimi.ingsw.client.cli.CLIHandler;
import it.polimi.ingsw.client.gui.GuiMain;
import javafx.application.Application;

/**
 * Starts a client program based on a chosen interface
 */
public class ClientMain {

    /**
     * Starts a CLI or GUI based interface of the client based on the parameters passed
     * @param args parameters passed when the program is launched
     */
    public static void main(String[] args) {
        final String ERROR = "usage:  sagrada  -g  [cli | gui]";

        if(args.length != 2){
            ClientLogger.println(ERROR);
            return;
        }
        ClientLogger.LogToFile("clientLog.log");

        if(args[0].equalsIgnoreCase("-g")) {
            switch (args[1].toLowerCase()) {
                case "cli":
                    (new CLIHandler()).start();
                    break;
                case "gui":
                    try{
                        Application.launch(GuiMain.class);
                    } catch (Exception e){
                        e.printStackTrace();
                        ClientLogger.println(MessageHandler.get("error-gui"));
                        (new CLIHandler()).start();
                    }
                    break;
                default:
                    ClientLogger.println(ERROR);
                    break;
            }
        }
        else
            ClientLogger.println(ERROR);
        System.exit(0);
    }
}

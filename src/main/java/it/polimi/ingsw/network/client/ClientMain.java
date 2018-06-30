package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.gui.GuiMain;
import javafx.application.Application;

import java.rmi.RemoteException;

public class ClientMain {
    public static void main(String[] args) {
        final String ERROR = "usage:  sagrada  -g  [cli | gui]";

        if(args.length != 2){
            ClientLogger.println(ERROR);
            return;
        }
        ClientLogger.LogToFile();

        if(args[0].equalsIgnoreCase("-g")) {
            switch (args[1].toLowerCase()) {
                case "cli":
                    (new CLIHandler()).start();
                    break;
                case "gui":
                    Application.launch(GuiMain.class);
                    break;
                default:
                    ClientLogger.println(ERROR);
            }
        }
        else
            ClientLogger.println(ERROR);
        System.exit(0);
    }
}

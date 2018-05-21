package it.polimi.ingsw.model.client;

public class ServerListener extends Thread {

    private Client client;
    private Connection server;

    ServerListener(Client client, Connection server){
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        String command;

        if(server instanceof ClientSocketHandler)
            while (true){

                command = ((ClientSocketHandler) server).socketReadLine();

                switch (command.split(" ")[0]) {
                    case "new_player":
                        client.addPlayers(command.substring(command.indexOf(" ") + 1).split(" "));
                        break;
                    case "quit":
                        client.removePlayer(command.substring(command.indexOf(" ") + 1));
                        break;
                    case "login":
                    case "verified":
                    case "failed":
                        ((ClientSocketHandler) server).continueLogin(command.split(" "));
                        break;
                    default: break;
                }
            }

    }
}

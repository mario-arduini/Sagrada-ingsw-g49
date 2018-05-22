package it.polimi.ingsw.model.client;

public class ServerListener extends Thread {

    private Client client;
    private Connection server;
    private boolean connected;

    ServerListener(Client client, Connection server){
        this.client = client;
        this.server = server;
        connected = true;
    }

    @Override
    public void run() {
        String command;

        if(server instanceof ClientSocketHandler)
            while (connected){

                command = ((ClientSocketHandler) server).socketReadLine();
                if(command != null) {
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
                        default:
                            break;
                    }
                }else
                    client.serverDisconnected();
            }

    }

    public void setConnected(boolean connected){
        this.connected = connected;
    }
}

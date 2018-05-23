package it.polimi.ingsw.network.client;

public class ServerListener extends Thread {

    private Client client;
    private ClientSocketHandler server;
    private boolean connected;

    ServerListener(Client client, ClientSocketHandler server){
        this.client = client;
        this.server = server;
        connected = true;
    }

    @Override
    public void run() {
        String command;

        while (connected){
            command = server.socketReadLine();
            if(command != null) {
                switch (command.split(" ")[0]) {
                    case "welcome":
                        client.welcomePlayer();
                        break;
                    case "new_player":
                        client.addPlayers(command.substring(command.indexOf(' ') + 1).split(" "));
                        break;
                    case "quit":
                        client.removePlayer(command.substring(command.indexOf(' ') + 1));
                        break;
                    case "login":
                    case "verified":
                    case "failed":
                        server.continueLogin(command.split(" "));
                        break;
                    default:
                        break;
                }
            }else {
                connected = false;
                client.serverDisconnected();     //server.close ??
            }
        }
    }

    void setConnected(boolean connected){
        this.connected = connected;
    }
}

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
        String[] split;

        if(server instanceof ClientSocketHandler)
            while (true){

                command = ((ClientSocketHandler) server).socketReadLine();
                split = command.split(" ");

                if(split[0].equals("new_player")) {
                    for (int i = 1; i < split.length; i++)
                        client.addPlayer(split[i]);
                    if (split.length > 2) {
                        System.out.println("Users playing:");
                        for (int i = 1; i < split.length; i++)
                            System.out.println(split[i]);
                    } else System.out.println(split[1] + " is now playing");
                }
                else if(split[0].equals("quit")) {
                    System.out.println(split[1] + " logged out");
                    client.removePlayer(split[1]);
                }
                else if(split[0].equals("login") || split[0].equals("verified") || split[0].equals("failed")) {
                    ((ClientSocketHandler) server).setCommand(split);
                }
            }

    }
}

package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameFlowHandler;
import it.polimi.ingsw.controller.GamesHandler;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.stream.Collectors;

public class SocketHandler implements Runnable, ConnectionHandler{
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String nickname;
    private boolean logged;
    private GameFlowHandler gameFlowHandler;


    public SocketHandler(Socket socket, GameFlowHandler gameFlowHandler) {
        this.socket = socket;
        this.gameFlowHandler = gameFlowHandler;
        this.logged = false;
    }

    public void run(){
        String message;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        socketPrintLine("welcome");
        while (!this.login()) {
            socketPrintLine("failed");
        }

        logged = true;

        while(logged){
            message = socketReadLine();
            if (message != null)
                switch (message.toLowerCase()) {
                    case "logout":
                        gameFlowHandler.logout();
                        logged = false;
                        socketClose();
                        break;
                    case "players":
                        List<String> players = gameFlowHandler.getPlayers();

                        socketPrintLine(players.toString());
                }
            else {
                Logger.print("Disconnected: " + nickname + " " + socket.getRemoteSocketAddress().toString());
                this.gameFlowHandler.disconnected();
                logged = false;
            }
        }



        //socketPrintLine("OK");
        socketClose();
    }

    @Override
    public void notifyLogin(String nickname) {
        socketPrintLine("new_player " + nickname);
    }

    @Override
    public void notifyLogin(List<String> nicknames){
        StringBuilder nicks = new StringBuilder();
        for (String nick:nicknames){
            nicks.append(" ");
            nicks.append(nick);
        }
        if (nicks.length() != 0)
            socketPrintLine("new_player" + nicks);
    }

    @Override
    public void notifyLogout(String nickname){
        socketPrintLine("quit " + nickname);
    }

    private boolean login() {
        String token;
        //socketPrint("login: ");

        this.nickname = socketReadLine();
        this.nickname = nickname.substring(nickname.indexOf(" ") + 1);

        token = gameFlowHandler.login(this.nickname, this);

        if (token != null) {
            socketPrintLine("login " + this.nickname + " " + token);
            return true;
        }
        socketPrintLine("login " + this.nickname + " token");

        //if (socketReadLine().toLowerCase().equalsIgnoreCase("yes")) {
            //socketPrint("token:");
        token = socketReadLine();
        token = token.substring(token.indexOf(" ") + 1);
        if (gameFlowHandler.reconnection(this.nickname, this, token)) {
            socketPrintLine("verified");
            return true;
        }
        //}
        return false;
    }

    private void socketPrintLine(String p) {
        output.println(p);
        output.flush();
    }

    private void socketPrint(String p) {
        output.print(p);
        output.flush();
    }

    private String socketReadLine(){
        try {
            return input.readLine();
        } catch(SocketException e){
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.print("Exception while reading.");
        }
        return null;
    }

    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.print("Exception while closing connection.");
        }
    }

    public void close(){
        if (this.logged){
            this.logged = false;
            socketClose();
        }
    }

    public String getRemoteAddress(){
        return socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void setGame(Game game){
        this.gameFlowHandler.setGame(game);
    }

}
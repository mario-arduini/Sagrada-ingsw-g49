package it.polimi.ingsw.socket;
import it.polimi.ingsw.socket.UsersHandler;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class SocketHandler implements Runnable, ConnectionHandler{
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String nickname;
    private UsersHandler usersHandler;

    public SocketHandler(Socket socket, UsersHandler usersHandler) {
        this.socket = socket;
        this.usersHandler = usersHandler;
    }

    public void run(){
        String message;
        boolean logged = false;

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
            switch (message.toLowerCase()) {
                case "logout":
                    usersHandler.logout(this.nickname);
                    logged = false;
                    socketClose();
                    break;

            }
        }


        //socketPrintLine("OK");
        socketClose();
    }

    @Override
    public void notifyPlayers(String nickname) {
        socketPrintLine("new_player " + nickname);
    }

    @Override
    public void notifyPlayers(List<String> nicknames){
        StringBuilder nicks = new StringBuilder();
        for (String nick:nicknames){
            nicks.append(" ");
            nicks.append(nick);
        }
        if (nicks.length() != 0)
            socketPrintLine("new_player" + nicks);
    }


    private boolean login() {
        String token;
        //socketPrint("login: ");

        this.nickname = socketReadLine();
        this.nickname = nickname.substring(nickname.indexOf(" ") + 1);

        token = usersHandler.login(this.nickname, this);

        if (token != null) {
            socketPrintLine("login " + this.nickname + " " + token);
            return true;
        }
        socketPrintLine("login " + this.nickname + " token");

        //if (socketReadLine().toLowerCase().equalsIgnoreCase("yes")) {
            //socketPrint("token:");
        token = socketReadLine();
        token = token.substring(token.indexOf(" ") + 1);
        if (usersHandler.login(this.nickname, this, token)) {
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
        } catch (IOException e) {
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
}
package it.polimi.ingsw.socket;
import it.polimi.ingsw.socket.UsersHandler;

import java.io.*;
import java.net.Socket;

public class SocketHandler implements Runnable{
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

        socketPrintLine("Welcome to LM49's Sagrada!");
        while (!this.login()) {
            socketPrintLine("Login failed, try again.");
        }

        socketPrintLine("OK");
        socketClose();
    }

    private boolean login() {
        String token;
        socketPrint("login: ");

        this.nickname = socketReadLine();

        token = usersHandler.login(this.nickname);

        if (token != null) {
            socketPrintLine("Welcome " + this.nickname + "\nYour login token is: " + token);
            return true;
        }
        socketPrint("Nickname already in use\nDo you have a recovery token?\n>");

        if (socketReadLine().toLowerCase().equalsIgnoreCase("yes")) {
            socketPrint("token:");
            if (usersHandler.loginLost(this.nickname, socketReadLine()))
                socketPrintLine("Welcome back " + this.nickname + "!");
            return true;
        }
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
        }
        return null;
    }

    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package it.polimi.ingsw.model.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocketHandler implements Connection {

    private BufferedReader input;
    private BufferedReader inputConsole;
    private PrintWriter output;
    private Socket socket;
    private Boolean flagLogin = false;
    private String[] splitCommand;

    ClientSocketHandler(String serverAddress, int serverPort) {
        inputConsole = new BufferedReader(new InputStreamReader(System.in));

        try {
            socket = new Socket(serverAddress, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            if (socketReadLine().equals("welcome"))
                System.out.println("Welcome to Sagrada!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean login(String nickname) {

        socketPrintLine("login " + nickname);

        while (!flagLogin)
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        if (splitCommand[2].equals("token")) {
            System.out.print("Insert your token: ");
            try {
                socketPrintLine("token " + inputConsole.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (flagLogin)
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            switch (splitCommand[0]) {
                case "failed":
                    System.out.println("Login failed, token is not correct");
                    return false;
                case "verified":
                    System.out.println("Login successful, token is correct");
                    return true;
            }

        }
        System.out.println("Login successful, your token is " + splitCommand[2]);
        flagLogin = false;
        return true;
    }

    public synchronized void setCommand(String[] splitCommand){
        flagLogin = !flagLogin;
        this.splitCommand = splitCommand;
        notifyAll();
    }



        /*while(!split[0].equals("login")) {
            loginResult = socketReadLine();
            split = loginResult.split(" ");
        }

        if (split[2].equals("token")) {
            System.out.print("Insert your token: ");
            try {
                socketPrintLine("token " + inputConsole.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                loginResult = socketReadLine();
                switch (loginResult) {
                    case "failed":
                        System.out.println("Login failed, token is not correct");
                        return false;
                    case "verified":
                        System.out.println("Login successful, token is correct");
                        return true;
                }
            }
        }
        System.out.println("Login successful, your token is " + split[2]);
        return true;*/


    public void logout(){
        socketPrintLine("logout");
        socketClose();
        System.out.println("Logged out");
    }

    private void socketPrintLine(String p) {
        output.println(p);
        output.flush();
    }

    String socketReadLine(){
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

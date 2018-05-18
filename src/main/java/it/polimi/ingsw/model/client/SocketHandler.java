package it.polimi.ingsw.model.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketHandler implements Connection{

    private BufferedReader input;
    private BufferedReader inputConsole;
    private PrintWriter output;
    private Socket socket;

    public SocketHandler() {
        inputConsole = new BufferedReader(new InputStreamReader(System.in));

        try{
            socket = new Socket("localhost", 1337);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void login(String nickname){
        if(socketReadLine().equals("welcome")) {

            System.out.println("Welcome to Sagrada!");
            socketPrintLine("login " + nickname);

            String loginResult = socketReadLine();

            String[] split = loginResult.split(" ");

            if (split[2].equals("token")) {
                System.out.print("Insert your token: ");
                try {
                    socketPrintLine("token " + inputConsole.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loginResult = socketReadLine();
                switch (loginResult) {
                    case "failed":
                        System.out.println("Login failed, token is not correct");
                        break;
                    case "verified":
                        System.out.println("Login successful, token is correct");
                        break;
                }
            } else System.out.println("Login successful, your token is " + split[2]);
        }
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

package it.polimi.ingsw.network.client.gui;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.client.ClientSocketHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GuiClient extends UnicastRemoteObject implements ClientInterface {

    public PasswordField password;
    public TextField nickname;
    public TextField address;
    public TextField port;
    public Button connect;
    public Button login;
    public Label status;

    private ClientSocketHandler socketHandler;

    private boolean addressOk,portOk,nicknameOk,passwordOk;
    private boolean serverConnected;
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public GuiClient() throws RemoteException {
        super();
        serverConnected = false;
    }

    public void initialize(){
        if(connect!=null){
            connect.setDisable(true);
            addressOk = false;
            portOk = false;

            address.textProperty().addListener((observable, oldValue, newValue) -> {
                if(PATTERN.matcher(newValue).matches()||newValue.trim().equals("localhost")){
                    addressOk = true;
                    address.setStyle("-fx-text-fill: black;");
                } else {
                    addressOk = false;
                    address.setStyle("-fx-text-fill: red;");
                }
                connect.setDisable(!(addressOk && portOk));
            });

            port.textProperty().addListener((observable, oldValue, newValue) -> {
                int portNumber;
                try{
                    portNumber = Integer.parseInt(newValue);
                    if(portNumber >= 1000 && portNumber <= 65535){
                        portOk = true;
                        port.setStyle("-fx-text-inner-color: black;");
                    } else {
                        portOk = false;
                        port.setStyle("-fx-text-inner-color: red;");
                    }
                } catch (NumberFormatException e){
                    portOk = false;
                    port.setStyle("-fx-text-inner-color: red;");
                } finally {
                    connect.setDisable(!(addressOk && portOk));
                }
            });
        } else if(login!=null){
            login.setDisable(true);
            nicknameOk = false;
            passwordOk = false;

            nickname.textProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue.length()>0){
                    nicknameOk = true;
                } else {
                    nicknameOk = false;
                }
                login.setDisable(!(nicknameOk && passwordOk));
            });

            password.textProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue.length()>3){
                    passwordOk = true;
                    password.setStyle("-fx-text-inner-color: black;");
                } else {
                    passwordOk = false;
                    password.setStyle("-fx-text-inner-color: red;");
                }
                login.setDisable(!(nicknameOk && passwordOk));
            });
        }

    }

    public void tryConnect(ActionEvent actionEvent) {

        try {
            socketHandler = new ClientSocketHandler(this, address.getCharacters().toString(), Integer.parseInt(port.getCharacters().toString()));
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // use wait and notify here!!
        while(!serverConnected){

        }

        Stage stage = (Stage) connect.getScene().getWindow();
        Parent root = null;
        try {
            URL path = GuiMain.class.getClassLoader().getResource("gui-views/login.fxml");
            root = FXMLLoader.load(path);
            stage.setTitle("Sagrada - Login");
            stage.setScene(new Scene(root));
            //stage.setResizable(false);
            //stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void trylogin(ActionEvent actionEvent) {
        Stage stage = (Stage) login.getScene().getWindow();
        stage.setResizable(true);
    }

    @Override
    public void welcomePlayer() throws RemoteException{
        serverConnected = true;
        //status.textProperty().set("Connected");
    }

    @Override
    public boolean isGameStarted() throws RemoteException {
        return false;
    }

    @Override
    public void setServerResult(boolean result) throws RemoteException {

    }

    @Override
    public void serverDisconnected() throws RemoteException {

    }

    @Override
    public void notifyLogin(String nickname) throws RemoteException {

    }

    @Override
    public void notifyLogin(List<String> nicknames) throws RemoteException {

    }

    @Override
    public void notifyLogout(String nickname) throws RemoteException {

    }

    @Override
    public void notifySchemas(List<Schema> schemas) throws RemoteException {

    }

    @Override
    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack) throws RemoteException {

    }

    @Override
    public void notifyOthersSchemas(Map<String, Schema> playersSchemas) throws RemoteException {

    }

    @Override
    public void notifyDicePlaced(String nickname, int row, int column, Dice dice) throws RemoteException {

    }

    @Override
    public void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack) throws RemoteException {

    }

    @Override
    public void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal) throws RemoteException {

    }

    @Override
    public void notifyReconInfo(HashMap<String, Window> windows, HashMap<String, Integer> favorToken, List<Dice> roundTrack) throws RemoteException {

    }

    @Override
    public void notifyEndGame(List<Score> scores) throws RemoteException {

    }

    @Override
    public Coordinate askDiceWindow(String prompt) throws RemoteException {
        return null;
    }

    @Override
    public Dice askDiceDraftPool(String prompt) throws RemoteException {
        return null;
    }

    @Override
    public int askDiceRoundTrack(String prompt) throws RemoteException {
        return 0;
    }

    @Override
    public boolean askIfPlus(String prompt) throws RemoteException {
        return false;
    }

    @Override
    public int askDiceValue(String prompt) throws RemoteException {
        return 0;
    }
}

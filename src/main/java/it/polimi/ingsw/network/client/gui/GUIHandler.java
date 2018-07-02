package it.polimi.ingsw.network.client.gui;

import com.sun.prism.GraphicsResource;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.client.ClientSocketHandler;
import it.polimi.ingsw.network.client.GraphicInterface;
import it.polimi.ingsw.network.client.model.GameSnapshot;
import javafx.application.Platform;
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

public class GUIHandler extends UnicastRemoteObject implements GraphicInterface {

    public PasswordField password;
    public TextField nickname;
    public TextField address;
    public TextField port;
    public Button connect;
    public Button login;
    public Label status;

    private Client client;

    private boolean addressOk,portOk,nicknameOk,passwordOk,isConnecting;
    private boolean serverConnected;
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public GUIHandler() throws RemoteException {
        super();
        serverConnected = false;
        try {
            this.client = new Client(this);
        }catch (RemoteException e){
            //LOGGER.warning(e.toString());
        }
    }

    public void initialize(){
        if(connect!=null) initConnectionScene();
        else if(login!=null) initLoginScene();

    }

    private void initConnectionScene(){
        connect.setDisable(true);
        addressOk = false;
        portOk = false;
        isConnecting = false;

        address.textProperty().addListener((observable, oldValue, newValue) -> {
            if(PATTERN.matcher(newValue).matches()||newValue.trim().equals("localhost")){
                addressOk = true;
                address.setStyle("-fx-text-fill: black;");
            } else {
                addressOk = false;
                address.setStyle("-fx-text-fill: red;");
            }
            connect.setDisable(!(addressOk && portOk)||isConnecting);
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
                connect.setDisable(!(addressOk && portOk)||isConnecting);
            }
        });
    }

    private void initLoginScene(){
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

    public void tryConnect(ActionEvent actionEvent) {

        status.textProperty().set("Connecting...");
        connect.setDisable(true);
        isConnecting = true;

        new Thread(() -> {
            client.setServerAddress(address.getText());
            client.setServerPort(Integer.parseInt(port.getText()));
            if(client.createConnection(Client.ConnectionType.SOCKET)){
                Platform.runLater(() -> {
                    status.textProperty().set("Connected bitches!");
                });
            } else {
                Platform.runLater(() -> {
                    status.textProperty().set("Connection failed! Try again");
                    connect.setDisable(false);
                    isConnecting = false;
                });
            }
        }).start();

    }

    public void trylogin(ActionEvent actionEvent) {
        Stage stage = (Stage) login.getScene().getWindow();
        stage.setResizable(true);
    }

    @Override
    public void printWaitingRoom() {

    }

    @Override
    public void printSchemaChoice(GameSnapshot gameSnapshot, List<Schema> schemas) {

    }

    @Override
    public void printGame(GameSnapshot gameSnapshot) {

    }

    @Override
    public void printMenu(GameSnapshot gameSnapshot) {

    }

    @Override
    public void notifyUsedToolCard(String player, String toolCard) {

    }

    @Override
    public void gameOver(List<Score> scores) {

    }

    @Override
    public void notifyServerDisconnected() {

    }

    @Override
    public boolean isWaiting() {
        return false;
    }

    @Override
    public boolean askIfPlus(String prompt) {
        return false;
    }

    @Override
    public Dice askDiceDraftPool(String prompt) {
        return null;
    }

    @Override
    public int askDiceRoundTrack(String prompt) {
        return 0;
    }

    @Override
    public Coordinate askDiceWindow(String prompt) {
        return null;
    }

    @Override
    public int askDiceValue(String prompt) {
        return 0;
    }
}

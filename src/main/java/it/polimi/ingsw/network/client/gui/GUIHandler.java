package it.polimi.ingsw.network.client.gui;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.client.GraphicInterface;
import it.polimi.ingsw.network.client.model.GameSnapshot;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.regex.Pattern;

public class GUIHandler extends UnicastRemoteObject implements GraphicInterface {

    @FXML private PasswordField password;
    @FXML private TextField nickname;
    @FXML private TextField address;
    @FXML private TextField port;
    @FXML private Button connect;
    @FXML private Button login;
    @FXML private Label status;
    @FXML private RadioButton rmi;
    @FXML private RadioButton socket;
    @FXML private Label portLabel;
    private ToggleGroup connectionRadioGroup;

    private Client client;
    private Stage stage;

    private boolean addressOk;
    private boolean portOk;
    private boolean lastPortOk;
    private boolean nicknameOk;
    private boolean passwordOk;
    private boolean isDoing;
    private Client.ConnectionType connectionType;

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public GUIHandler() throws RemoteException {
        super();
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
        isDoing = false;

        connectionRadioGroup = new ToggleGroup();

        rmi.setToggleGroup(connectionRadioGroup);
        rmi.setUserData("rmi");
        socket.setToggleGroup(connectionRadioGroup);
        socket.setUserData("socket");
        socket.setSelected(true);
        connectionType = Client.ConnectionType.SOCKET;

        address.textProperty().addListener((observable, oldValue, newValue) -> {
            if(PATTERN.matcher(newValue).matches()||newValue.trim().equals("localhost")){
                addressOk = true;
                address.setStyle("-fx-text-fill: black;");
            } else {
                addressOk = false;
                address.setStyle("-fx-text-fill: red;");
            }
            connect.setDisable(!(addressOk && portOk)|| isDoing);
        });

        port.textProperty().addListener((observable, oldValue, newValue) -> {
            int portNumber;
            try{
                portNumber = Integer.parseInt(newValue);
                if(portNumber >= 10000 && portNumber <= 65535){
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
                connect.setDisable(!(addressOk && portOk)|| isDoing);
            }
        });

        connectionRadioGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.getUserData().toString().equals("socket")){
                connectionType = Client.ConnectionType.SOCKET;
                port.setVisible(true);
                portLabel.setVisible(true);
                portOk = lastPortOk;
                connect.setDisable(!(addressOk && portOk)|| isDoing);
            }
            else {
                connectionType = Client.ConnectionType.RMI;
                port.setVisible(false);
                portLabel.setVisible(false);
                lastPortOk = false;
                portOk = true;
                connect.setDisable(!(addressOk && portOk)|| isDoing);
            }
        });
    }

    private void initLoginScene(){
        login.setDisable(true);
        nicknameOk = false;
        passwordOk = false;
        isDoing = false;

        nickname.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length()>0){
                nicknameOk = true;
            } else {
                nicknameOk = false;
            }
            login.setDisable(!(nicknameOk && passwordOk)&&isDoing);
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length()>3){
                passwordOk = true;
                password.setStyle("-fx-text-inner-color: black;");
            } else {
                passwordOk = false;
                password.setStyle("-fx-text-inner-color: red;");
            }
            login.setDisable(!(nicknameOk && passwordOk)&&isDoing);
        });
    }

    public void tryConnect(ActionEvent actionEvent) {

        status.textProperty().set("Connecting...");
        connect.setDisable(true);
        isDoing = true;

        new Thread(() -> {
            client.setServerAddress(address.getText());
            if(connectionType.equals(Client.ConnectionType.SOCKET)) client.setServerPort(Integer.parseInt(port.getText()));
            if(client.createConnection(connectionType)){
                Platform.runLater(() -> {
                    try {
                        stage = (Stage) connect.getScene().getWindow();
                        URL path = GUIHandler.class.getClassLoader().getResource("gui-views/login.fxml");
                        FXMLLoader fxmlLoader = new FXMLLoader(path);
                        Parent root = fxmlLoader.load();
                        GUIHandler controller = (GUIHandler) fxmlLoader.getController();
                        controller.passClient(client);
                        stage.setScene(new Scene(root));
                        stage.setTitle("Sagrada - Login");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
            } else {
                Platform.runLater(() -> {
                    status.textProperty().set("Connection failed! Try again");
                    connect.setDisable(false);
                    isDoing = false;
                });
            }
        }).start();

    }

    public void trylogin(ActionEvent actionEvent) {
        status.textProperty().set("Logging In...");
        login.setDisable(true);
    }

    public void passClient(Client client){
        this.client = client;
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

    @Override
    public void wakeUp(boolean serverResult) {
        
    }
}

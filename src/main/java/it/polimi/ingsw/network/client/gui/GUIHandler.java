package it.polimi.ingsw.network.client.gui;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.client.GraphicInterface;
import it.polimi.ingsw.network.client.model.GameSnapshot;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GUIHandler extends UnicastRemoteObject implements GraphicInterface {

    @FXML private Button schema0;
    @FXML private Button schema1;
    @FXML private Button schema2;
    @FXML private Button schema3;
    @FXML private GridPane schemaChoice;
    @FXML private Label passwordLabel;
    @FXML private Label nicknameLabel;
    @FXML private Label waitingRoom;
    @FXML private ListView playerListView;
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
    @FXML private ImageView privateGoal;
    @FXML private ImageView publicGoal1;
    @FXML private ImageView publicGoal2;
    @FXML private ImageView publicGoal3;

    private ToggleGroup connectionRadioGroup;
    ObservableList<String> playerList = FXCollections.observableArrayList();
    private List<GridPane> schemasGrid;

    private Client client;
    private List<Schema> schemas;

    private boolean addressOk;
    private boolean portOk;
    private boolean lastPortOk;
    private boolean nicknameOk;
    private boolean passwordOk;
    private boolean isConnecting;
    private boolean isLogging;
    private boolean choosingSchema;
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
        else if(schemaChoice!=null) initSchemaChoiceScene();

    }

    private void initConnectionScene(){
        connect.setDisable(true);
        addressOk = false;
        portOk = false;
        isConnecting = false;

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
            connect.setDisable(!(addressOk && portOk)|| isConnecting);
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
                connect.setDisable(!(addressOk && portOk)|| isConnecting);
            }
        });

        connectionRadioGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.getUserData().toString().equals("socket")){
                connectionType = Client.ConnectionType.SOCKET;
                port.setVisible(true);
                portLabel.setVisible(true);
                portOk = lastPortOk;
                connect.setDisable(!(addressOk && portOk)|| isConnecting);
            }
            else {
                connectionType = Client.ConnectionType.RMI;
                port.setVisible(false);
                portLabel.setVisible(false);
                lastPortOk = false;
                portOk = true;
                connect.setDisable(!(addressOk && portOk)|| isConnecting);
            }
        });
    }

    private void initLoginScene(){
        waitingRoom.setVisible(false);
        playerListView.setVisible(false);
        login.setDisable(true);
        nicknameOk = false;
        passwordOk = false;
        isLogging = false;

        nickname.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length()>0){
                nicknameOk = true;
            } else {
                nicknameOk = false;
            }
            login.setDisable(!(nicknameOk && passwordOk)&& isLogging);
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length()>3){
                passwordOk = true;
                password.setStyle("-fx-text-inner-color: black;");
            } else {
                passwordOk = false;
                password.setStyle("-fx-text-inner-color: red;");
            }
            login.setDisable(!(nicknameOk && passwordOk)&& isLogging);
        });
    }

    private void initSchemaChoiceScene(){
        choosingSchema = false;
        schemasGrid = new ArrayList<>();
        schemasGrid.add((GridPane) schemaChoice.getChildren().get(4));
        schemasGrid.add((GridPane) schemaChoice.getChildren().get(5));
        schemasGrid.add((GridPane) schemaChoice.getChildren().get(6));
        schemasGrid.add((GridPane) schemaChoice.getChildren().get(7));
        ColumnConstraints schemacc = new ColumnConstraints();
        schemacc.setPercentWidth(20.0);
        RowConstraints schemarc = new RowConstraints();
        schemarc.setPercentHeight(25.0);

        schemasGrid.forEach(g -> {
            g.getColumnConstraints().addAll(schemacc,schemacc,schemacc,schemacc,schemacc);
            g.getRowConstraints().addAll(schemarc,schemarc,schemarc,schemarc);
            g.prefHeightProperty().bind(g.widthProperty().multiply(0.8));
        });

        schema0.setOnAction(e -> {
            disableSchemaChoice();
            client.sendSchemaChoice(0);
        });
        schema1.setOnAction(e -> {
            disableSchemaChoice();
            client.sendSchemaChoice(1);
        });
        schema2.setOnAction(e -> {
            disableSchemaChoice();
            client.sendSchemaChoice(2);
        });
        schema3.setOnAction(e -> {
            disableSchemaChoice();
            client.sendSchemaChoice(3);
        });
    }

    private void disableSchemaChoice(){
        choosingSchema = true;
        schema0.setDisable(true);
        schema1.setDisable(true);
        schema2.setDisable(true);
        schema3.setDisable(true);
    }

    private void enableSchemaChoice(){
        choosingSchema = false;
        schema0.setDisable(false);
        schema1.setDisable(false);
        schema2.setDisable(false);
        schema3.setDisable(false);
    }

    private void populateSchemaChoice(){
        privateGoal.imageProperty().set( GuiMain.getGoalImage(client.getGameSnapshot().getPlayer().getPrivateGoal()) );
        List<String> publicGoalNames = client.getGameSnapshot().getPublicGoals();
        publicGoal1.imageProperty().set( GuiMain.getGoalImage(publicGoalNames.get(0)) );
        publicGoal2.imageProperty().set( GuiMain.getGoalImage(publicGoalNames.get(1)) );
        publicGoal3.imageProperty().set( GuiMain.getGoalImage(publicGoalNames.get(2)) );

        for(int i=0;i<schemasGrid.size();i++) createSchema(schemas.get(i),schemasGrid.get(i));

        schema0.setText("Choose "+schemas.get(0).getName());
        schema1.setText("Choose "+schemas.get(1).getName());
        schema2.setText("Choose "+schemas.get(2).getName());
        schema3.setText("Choose "+schemas.get(3).getName());

    }

    private void populateGame() {
    }

    private void createSchema(Schema schema,GridPane grid){
        Constraint constraint;
        for(int r = 0;r<Window.ROW;r++)
            for (int c = 0;c<Window.COLUMN;c++){
                constraint = schema.getConstraint(r,c);
                if(constraint!=null){
                    Pane cell = (Pane)grid.getChildren().get(r*Window.COLUMN+c);
                    if(constraint.getColor()!=null){
                        cell.setStyle("-fx-background-color: "+constraint.getColor()+";");
                    } else {
                        cell.getChildren().add(new Label(constraint.getNumber().toString()));
                    }
                }
            }
    }

    public void tryConnect(ActionEvent actionEvent) {

        status.textProperty().set("Connecting...");
        connect.setDisable(true);
        isConnecting = true;

        new Thread(() -> {
            client.setServerAddress(address.getText());
            if(connectionType.equals(Client.ConnectionType.SOCKET)) client.setServerPort(Integer.parseInt(port.getText()));
            if(client.createConnection(connectionType)){
                Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) connect.getScene().getWindow();
                        URL path = GUIHandler.class.getClassLoader().getResource("gui-views/login.fxml");
                        FXMLLoader fxmlLoader = new FXMLLoader(path);
                        Parent root = fxmlLoader.load();
                        GUIHandler controller = (GUIHandler) fxmlLoader.getController();
                        controller.passClient(client);
                        client.setHandler(controller);
                        stage.setScene(new Scene(root));
                        stage.setTitle("Sagrada - Login");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
            } else {
                Platform.runLater(() -> {
                    status.textProperty().set("Connection failed!");
                    connect.setDisable(false);
                    isConnecting = false;
                });
            }
        }).start();

    }

    public void trylogin(ActionEvent actionEvent) {
        status.textProperty().set("Logging In...");
        login.setDisable(true);
        isLogging = true;
        client.login(nickname.getText(),password.getText());
    }

    public void passClient(Client client){
        this.client = client;
    }

    public void passSchemas(List<Schema> schemas){
        this.schemas = schemas;
    }

    @Override
    public void printWaitingRoom() {
        Platform.runLater(() -> {
            playerList.clear();
            client.getGameSnapshot().getOtherPlayers().forEach(nick -> playerList.add(nick.getNickname()));
            playerList.add(client.getGameSnapshot().getPlayer().getNickname());
        });
        wakeUp(true);
    }

    @Override
    public void printSchemaChoice(GameSnapshot gameSnapshot, List<Schema> schemas) {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) status.getScene().getWindow();
                URL path = GUIHandler.class.getClassLoader().getResource("gui-views/schema-choice.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(path);
                Parent root = fxmlLoader.load();
                GUIHandler controller = (GUIHandler) fxmlLoader.getController();
                controller.passClient(client);
                controller.passSchemas(schemas);
                controller.populateSchemaChoice();
                client.setHandler(controller);
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.setTitle("Sagrada - Schema Choice");
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
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
        if(isLogging) handleLogin(serverResult);
        if(choosingSchema) handleSchema(serverResult);
    }

    @Override
    public void interruptInput(){

    }

    public void handleLogin(boolean serverResult){
        Platform.runLater(() -> {
            if(serverResult){
                isLogging = false;
                status.textProperty().set("Logged in");
                password.setVisible(false);
                passwordLabel.setVisible(false);
                nickname.setVisible(false);
                nicknameLabel.setVisible(false);
                login.setVisible(false);

                waitingRoom.setVisible(true);
                playerListView.setItems(playerList);
                playerListView.setVisible(true);
            } else {
                status.textProperty().set("Wrong password!");
                login.setDisable(false);
                isLogging = false;
            }
        });
    }

    private void handleSchema(boolean serverResult){
        Platform.runLater(() -> {
            if(serverResult){
                choosingSchema = false;
                try {
                    Stage stage = (Stage) schema0.getScene().getWindow();
                    URL path = GUIHandler.class.getClassLoader().getResource("gui-views/game.fxml");
                    FXMLLoader fxmlLoader = new FXMLLoader(path);
                    Parent root = fxmlLoader.load();
                    GUIHandler controller = (GUIHandler) fxmlLoader.getController();
                    controller.passClient(client);
                    controller.populateGame();
                    client.setHandler(controller);
                    stage.setScene(new Scene(root));
                    stage.setTitle("Sagrada - Game");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                enableSchemaChoice();
            }
        });
    }
}

package it.polimi.ingsw.network.client.gui;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.client.GraphicInterface;
import it.polimi.ingsw.network.client.MessageHandler;
import it.polimi.ingsw.network.client.ServerReconnectedException;
import it.polimi.ingsw.network.client.model.GameSnapshot;
import it.polimi.ingsw.network.client.model.PlayerSnapshot;
import it.polimi.ingsw.network.client.model.ToolCard;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class GUIHandler extends UnicastRemoteObject implements GraphicInterface {

    @FXML private VBox scoresBox;
    @FXML private HBox roundTrack;
    @FXML private FlowPane toolBox;
    @FXML private Label info;
    @FXML private Button passButton;
    @FXML private Button tool1;
    @FXML private Button tool2;
    @FXML private Button tool3;
    @FXML private GridPane toolGrid;
    @FXML private GridPane otherGrid;
    @FXML private GridPane goalGrid;
    @FXML private TabPane tabPane;
    @FXML private HBox draftPool;
    @FXML private GridPane gameScene;
    @FXML private ImageView toolCard1;
    @FXML private ImageView toolCard2;
    @FXML private ImageView toolCard3;
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
    private ObservableList<String> playerList = FXCollections.observableArrayList();
    private SagradaGridPane playerGrid;
    private List<SagradaGridPane> otherPlayerGrids;

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
    private boolean gameStarted;
    private boolean usingToolcard;
    private Client.ConnectionType connectionType;

    private Dice askedDraft;
    private Coordinate askedCoordinate;
    private int askedNumber;
    private boolean askedBool;
    private String tmpStyle;
    private int toolcardInUse;

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
        else if(gameScene!=null) initGameScene();

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

        schema0.setOnAction(e -> {
            disableSchemaChoice();
            try {
                client.sendSchemaChoice(0);
            } catch (ServerReconnectedException e1) {
                e1.printStackTrace();
            }
        });
        schema1.setOnAction(e -> {
            disableSchemaChoice();
            try {
                client.sendSchemaChoice(1);
            } catch (ServerReconnectedException e1) {
                e1.printStackTrace();
            }
        });
        schema2.setOnAction(e -> {
            disableSchemaChoice();
            try {
                client.sendSchemaChoice(2);
            } catch (ServerReconnectedException e1) {
                e1.printStackTrace();
            }
        });
        schema3.setOnAction(e -> {
            disableSchemaChoice();
            try {
                client.sendSchemaChoice(3);
            } catch (ServerReconnectedException e1) {
                e1.printStackTrace();
            }
        });
    }

    private void initGameScene(){
        goalGrid.prefWidthProperty().bind(tabPane.widthProperty());
        otherGrid.prefWidthProperty().bind(tabPane.widthProperty());
        toolGrid.prefWidthProperty().bind(tabPane.widthProperty());
        tabPane.maxHeightProperty().bind(tabPane.widthProperty().multiply(.55));
        draftPool.setSpacing(draftPool.getHeight()*0.1);
        draftPool.spacingProperty().bind(draftPool.heightProperty().multiply(0.1));

        gameStarted = true;
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

        for(int i=0;i<schemas.size();i++){
            SagradaGridPane schemaGrid = new SagradaGridPane();
            schemaGrid.initProperty();
            schemaGrid.setSchema(schemas.get(i));
            schemaChoice.add(schemaGrid,i,1);
        }

        schema0.setText("Choose "+schemas.get(0).getName());
        schema1.setText("Choose "+schemas.get(1).getName());
        schema2.setText("Choose "+schemas.get(2).getName());
        schema3.setText("Choose "+schemas.get(3).getName());

    }

    private void populateGame() {
        List<String> publicGoalNames = client.getGameSnapshot().getPublicGoals();
        publicGoal1.imageProperty().set( GuiMain.getGoalImage(publicGoalNames.get(0)) );
        publicGoal2.imageProperty().set( GuiMain.getGoalImage(publicGoalNames.get(1)) );
        publicGoal3.imageProperty().set( GuiMain.getGoalImage(publicGoalNames.get(2)) );

        List<ToolCard> toolCardNames = client.getGameSnapshot().getToolCards();
        toolCard1.imageProperty().set( GuiMain.getToolImage(toolCardNames.get(0).getName()) );
        toolCard2.imageProperty().set( GuiMain.getToolImage(toolCardNames.get(1).getName()) );
        toolCard3.imageProperty().set( GuiMain.getToolImage(toolCardNames.get(2).getName()) );

        playerGrid = new SagradaGridPane();
        playerGrid.initProperty();
        playerGrid.setSchema(client.getGameSnapshot().getPlayer().getWindow().getSchema());
        playerGrid.passController(this);
        gameScene.add(playerGrid,1,1);

        otherPlayerGrids = new ArrayList<>();

        info.prefWidthProperty().bind(toolBox.widthProperty());
        info.setStyle("-fx-border-width : 5; -fx-border-color: black; -fx-border-style:solid;");
        info.setText(MessageHandler.get("info-welcome"));

        int i = 0;
        for(PlayerSnapshot player : client.getGameSnapshot().getOtherPlayers()){
            SagradaGridPane pGrid = new SagradaGridPane();
            pGrid.initProperty();
            pGrid.setSchema(player.getWindow().getSchema());
            otherPlayerGrids.add(pGrid);
            otherGrid.add(pGrid,i,1);
            i++;
        }

        List<ToolCard> tools = client.getGameSnapshot().getToolCards();
        tool1.setText(tools.get(0).getName()+ (tools.get(0).getUsed() ? " (2)" : " (1)"));
        tool1.setOnAction(event -> useToolCard(0));
        tool2.setText(tools.get(1).getName()+ (tools.get(1).getUsed() ? " (2)" : " (1)"));
        tool2.setOnAction(event -> useToolCard(1));
        tool3.setText(tools.get(2).getName()+ (tools.get(2).getUsed() ? " (2)" : " (1)"));
        tool3.setOnAction(event -> useToolCard(2));

        printGame(client.getGameSnapshot());
        printMenu(client.getGameSnapshot());
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
        try {
            client.login(nickname.getText(),password.getText());
        } catch (ServerReconnectedException e) {
            e.printStackTrace();
        }
    }

    private void useToolCard(int toolNumber){
        List<ToolCard> tools = client.getGameSnapshot().getToolCards();

        System.out.println(Thread.currentThread().getId());
        client.useToolCard(tools.get(toolNumber).getName());


        tool1.setDisable(false);
        tool2.setDisable(false);
        tool3.setDisable(false);

        usingToolcard = true;
        toolcardInUse = toolNumber;
    }

    public Client getClient(){
        return client;
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
        if(gameStarted){
            Platform.runLater(()->{
                roundTrack.getChildren().clear();
                List<Dice> track = client.getGameSnapshot().getRoundTrack();
                for(int i = 0;i<track.size();i++){
                    DicePane dp = new DicePane(track.get(i),i);
                    dp.bindDimension(roundTrack.heightProperty());
                    roundTrack.getChildren().add(dp);
                }

                playerGrid.updateWindow(client.getGameSnapshot().getPlayer().getWindow());
                for(int i=0;i<otherPlayerGrids.size();i++){
                    otherPlayerGrids.get(i).updateWindow(client.getGameSnapshot().getOtherPlayers().get(i).getWindow());
                }

                List<ToolCard> tools = client.getGameSnapshot().getToolCards();
                tool1.setText(tools.get(0).getName()+ (tools.get(0).getUsed() ? " (2)" : " (1)"));
                tool2.setText(tools.get(1).getName()+ (tools.get(1).getUsed() ? " (2)" : " (1)"));
                tool3.setText(tools.get(2).getName()+ (tools.get(2).getUsed() ? " (2)" : " (1)"));
            });
        }
    }

    @Override
    public void printMenu(GameSnapshot gameSnapshot) {
        if(gameStarted){
            Platform.runLater(()->{
                draftPool.getChildren().clear();
                boolean myTurn = client.getGameSnapshot().getPlayer().isMyTurn();
                boolean dicePlaced = client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted();
                boolean toolUsed = client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed();
                List<Dice> pool = client.getGameSnapshot().getDraftPool();
                for(int i = 0;i<pool.size();i++){
                    DicePane dp = new DicePane(pool.get(i),i+1);
                    dp.bindDimension(draftPool.heightProperty());
                    if(myTurn&&!dicePlaced){
                        dp.setDraggable();
                        dp.setCursor(Cursor.MOVE);
                    }
                    draftPool.getChildren().add(dp);
                }
                passButton.setDisable(!myTurn);
                tool1.setDisable(!myTurn||toolUsed);
                tool2.setDisable(!myTurn||toolUsed);
                tool3.setDisable(!myTurn||toolUsed);

                if(myTurn){
                    if(toolUsed) info.setText(MessageHandler.get("info-tool-used"));
                    else if(dicePlaced) info.setText(MessageHandler.get("info-dice-placed"));
                    else info.setText(MessageHandler.get("info-your-turn"));
                } else {
                    info.setText(MessageHandler.get("info-not-turn"));
                }

            });
        }
    }

    @Override
    public void notifyUsedToolCard(String player, String toolCard) {

    }

    @Override
    public void gameOver(List<Score> scores) {
        Platform.runLater(() -> {
            try {
                Stage stage;
                if(gameScene!=null) stage = (Stage) gameScene.getScene().getWindow();
                else stage = (Stage) schemaChoice.getScene().getWindow();
                URL path = GUIHandler.class.getClassLoader().getResource("gui-views/gameover.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(path);
                Parent root = fxmlLoader.load();
                GUIHandler controller = (GUIHandler) fxmlLoader.getController();
                controller.passClient(client);
                controller.showScores(scores);
                client.setHandler(controller);
                stage.setScene(new Scene(root));
                stage.setTitle("Sagrada - Gameover");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void showScores(List<Score> scores){
        for(Score score : scores){
            Label label = new Label(score.getPlayer()+" - "+score.getTotalScore());
            if(score.getPlayer().equals(client.getGameSnapshot().getPlayer().getNickname())){
                label.setStyle("-fx-text-fill : green;");
            }
            label.setFont(new Font(20));
            scoresBox.getChildren().add(label);
        }
    }

    @Override
    public void notifyServerDisconnected() {

    }

    @Override
    public boolean askIfPlus(String prompt, boolean rollback) {
        Platform.runLater(()->{

            ButtonType plus = new ButtonType("+", ButtonBar.ButtonData.YES);
            ButtonType minus = new ButtonType("-", ButtonBar.ButtonData.NO);
            Alert askBool = new Alert(Alert.AlertType.CONFIRMATION, MessageHandler.get(prompt), plus,minus);

            askBool.showAndWait();

            if(askBool.getResult() == plus) askedBool = true;
            else askedBool = false;

            synchronized (this) {
                this.notify();
            }

        });

        try {
            synchronized (this){
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Returning "+askedCoordinate);
        return askedBool;
    }

    @Override
    public Dice askDiceDraftPool(String prompt, boolean rollback) {

        Platform.runLater(()->{
            info.setText(MessageHandler.get(prompt));
            draftPool.setStyle("-fx-border-color:red; -fx-border-width:3; -fx-border-style:dashed;");
            draftPool.getChildren().forEach(child -> {
                child.setOnMouseClicked(event -> {
                    DicePane dp = (DicePane) event.getSource();
                    askedDraft = dp.dice;
                    removeFromDraftIfNecessary(dp);
                    draftPool.setStyle("");
                    synchronized (this) {

                        System.out.println("Notifying on "+this.getClass());
                        this.notify();
                    }
                });
            });
        });

        System.out.println(Thread.currentThread().getId());

        try {
            synchronized (this){
                System.out.println("Waiting on "+this.getClass());
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Returning "+askedDraft);
        return askedDraft;
    }

    @Override
    public int askDiceRoundTrack(String prompt, boolean rollback) {
        Platform.runLater(()->{
            info.setText(MessageHandler.get(prompt));
            roundTrack.setStyle("-fx-border-color:red; -fx-border-width:3; -fx-border-style:dashed;");
            roundTrack.getChildren().forEach(child -> {
                child.setOnMouseClicked(event -> {
                    DicePane dp = (DicePane) event.getSource();
                    roundTrack.setStyle("");
                    askedNumber = dp.getIdx();
                    synchronized (this) {
                        this.notify();
                    }
                });
            });
        });

        try {
            synchronized (this){
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Returning "+askedNumber);
        return askedNumber;
    }

    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) {

        Platform.runLater(()->{
            info.setText(MessageHandler.get(prompt));
            tmpStyle = playerGrid.getStyle();
            playerGrid.setStyle(tmpStyle + "-fx-border-color:red; -fx-border-width:3; -fx-border-style:dashed;");
            playerGrid.getChildren().forEach(child -> {
                System.out.println(child.getClass());
                child.setOnMouseClicked(event -> {
                    playerGrid.setStyle(tmpStyle);
                    Node source = (Node) event.getSource();
                    int row = GridPane.getRowIndex(source);
                    int col = GridPane.getColumnIndex(source);
                    askedCoordinate = new Coordinate(row+1,col+1);
                    synchronized (this) {
                        this.notify();
                    }
                });
            });
        });

        try {
            synchronized (this){
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Returning "+askedCoordinate);
        return askedCoordinate;

    }

    @Override
    public int askDiceValue(String prompt, boolean rollback) {

        Platform.runLater(()->{

            ButtonType button1 = new ButtonType("1");
            ButtonType button2 = new ButtonType("2");
            ButtonType button3 = new ButtonType("3");
            ButtonType button4 = new ButtonType("4");
            ButtonType button5 = new ButtonType("5");
            ButtonType button6 = new ButtonType("6");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Dice Value");
            alert.setHeaderText(MessageHandler.get(prompt));

            alert.getButtonTypes().setAll(button1,button2,button3,button4,button5,button6);

            boolean choose = false;
            while (!choose){
                choose = true;
                alert.showAndWait();
                if (alert.getResult() == button1){
                    askedNumber = 1;
                } else if (alert.getResult() == button2){
                    askedNumber = 2;
                } else if (alert.getResult() == button3){
                    askedNumber = 3;
                } else if (alert.getResult() == button4){
                    askedNumber = 4;
                } else if (alert.getResult() == button5){
                    askedNumber = 5;
                } else if (alert.getResult() == button6){
                    askedNumber = 6;
                } else choose = false;
            }

            synchronized (this) {
                this.notify();
            }

        });

        try {
            synchronized (this){
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Returning "+askedNumber);
        return askedNumber;

    }

    @Override
    public int askMoveNumber(String prompt, int n, boolean rollback){
        Platform.runLater(()->{

            List<ButtonType> buttonNumbers = new ArrayList<>();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("How many to move");
            alert.setHeaderText(MessageHandler.get(prompt));

            for(Integer i = 1;i <= n;i++){
                ButtonType button = new ButtonType(i.toString());
                buttonNumbers.add(button);
                alert.getButtonTypes().add(button);
            }

            boolean choose = false;

            while (!choose){
                alert.showAndWait();
                for(Integer i = 0;i < n;i++){
                    if(alert.getResult() == buttonNumbers.get(i)){
                        askedNumber = i+1;
                        choose = true;
                    }
                }
            }

            synchronized (this) {
                this.notify();
            }

        });

        try {
            synchronized (this){
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Returning "+askedNumber);
        return askedNumber;
    }

    @Override
    public void printDice(Dice dice){
        Platform.runLater(() -> {
            DicePane dp = new DicePane(dice);
            dp.bindDimension(draftPool.heightProperty());
            dp.setStyle(dp.getStyle() + "-fx-border-color:red; -fx-border-style:dashed;");
            draftPool.getChildren().add(dp);
        });
    }

    @Override
    public void wakeUp(boolean serverResult) {
        if(isLogging) handleLogin(serverResult);
        if(choosingSchema) handleSchema(serverResult);
        if(playerGrid!=null&&playerGrid.isPlacingDice()&&!serverResult){
            Platform.runLater(() ->{
                info.setText(MessageHandler.get("info-dice-bad"));
            });
        }
        if(usingToolcard&&!serverResult){
            Platform.runLater(() -> {
                info.setText(MessageHandler.get("info-tool-fail"));
                tool1.setDisable(false);
                tool2.setDisable(false);
                tool3.setDisable(false);
            });
        }
    }

    @Override
    public void alertDiceInDraftPool(Dice dice){

    }

    @Override
    public void interruptInput(){

    }

    public void handleLogin(boolean serverResult){
        Platform.runLater(() -> {
            if(serverResult){
                setWaitingRoom();
                Stage stage = (Stage) login.getScene().getWindow();
                stage.setOnCloseRequest( event ->
                {
                    try {
                        client.logout();
                    } catch (ServerReconnectedException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                status.textProperty().set("Wrong password!");
                login.setDisable(false);
                isLogging = false;
            }
        });
    }

    public void setWaitingRoom(){
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

    public void pass(ActionEvent actionEvent) {
        try {
            client.pass();
        } catch (ServerReconnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setToolCardNotCompleted(String toolCard){
        Platform.runLater(() -> {
            Stage stage = (Stage) login.getScene().getWindow();
            URL path = GUIHandler.class.getClassLoader().getResource("gui-views/game.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(path);
            Parent root = null;
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            GUIHandler controller = (GUIHandler) fxmlLoader.getController();
            controller.passClient(client);
            controller.populateGame();
            client.setHandler(controller);
            stage.setScene(new Scene(root));
            stage.setTitle("Sagrada - Game");
        });
    }

    private void removeFromDraftIfNecessary(DicePane dp){
        String toolName = client.getGameSnapshot().getToolCards().get(toolcardInUse).getName();
        if(toolName.equals("Pennello per pasta salda")||toolName.equals("Diluente per pasta salda")||toolName.equals("Pinza sgrossatrice")){
            draftPool.getChildren().remove(dp);
        }
    }

    public void newGame(ActionEvent actionEvent) {
        try {
            client.newGame();
        } catch (ServerReconnectedException e) {
            e.printStackTrace();
        }

        try {
            Stage stage = (Stage) scoresBox.getScene().getWindow();
            URL path = GUIHandler.class.getClassLoader().getResource("gui-views/login.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(path);
            Parent root = fxmlLoader.load();
            GUIHandler controller = (GUIHandler) fxmlLoader.getController();
            controller.passClient(client);
            controller.setWaitingRoom();
            controller.printWaitingRoom();
            client.setHandler(controller);

            stage.setScene(new Scene(root));
            stage.setTitle("Sagrada - Waiting Room");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

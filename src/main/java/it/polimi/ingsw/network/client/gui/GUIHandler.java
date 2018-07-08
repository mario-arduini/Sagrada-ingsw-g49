package it.polimi.ingsw.network.client.gui;

import it.polimi.ingsw.controller.exceptions.RollbackException;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class GUIHandler extends UnicastRemoteObject implements GraphicInterface {

    @FXML private HBox topDisplay;
    @FXML private Label nameLabel;
    @FXML private HBox favToken;
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

    private ObservableList<String> playerList = FXCollections.observableArrayList();
    private transient SagradaGridPane playerGrid;
    private transient List<SagradaGridPane> otherPlayerGrids;
    private List<Label> otherPlayerLabels;
    private List<HBox> otherPlayerTokens;
    private Button rollbackButton;

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
    private Client.ConnectionType connectionType;
    private Stage stage;

    private Dice askedDraft;
    private Coordinate askedCoordinate;
    private int askedNumber;
    private boolean askedBool;
    private int toolCardInUse;
    private boolean backed;
    private String tmpStyle;

    private static final String RED_TEXT = "-fx-text-inner-color: red;";
    private static final String BLACK_TEXT = "-fx-text-inner-color: black;";
    private static final String RED_DASHED_BORD = "-fx-border-color:red; -fx-border-width:3; -fx-border-style:dashed;";
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());


    private boolean alertDiceUnplacable;
    private transient DicePane activeDice;

    public GUIHandler() throws RemoteException {
        super();
        try {
            this.client = new Client(this);
        }catch (RemoteException e){
            LOGGER.warning(e.toString());
        }
    }

    /***
     * Call when every scene using this controller is loaded
     */
    public void initialize(){
        if(connect!=null) initConnectionScene();
        else if(login!=null) initLoginScene();
        else if(schemaChoice!=null) initSchemaChoiceScene();
        else if(gameScene!=null) initGameScene();
    }

    /***
     * Init the Connection Scene, setup flags and event listener for text input
     */
    private void initConnectionScene(){
        connect.setDisable(true);
        addressOk = false;
        portOk = false;
        isConnecting = false;

        ToggleGroup connectionRadioGroup = new ToggleGroup();

        rmi.setToggleGroup(connectionRadioGroup);
        rmi.setUserData("rmi");
        socket.setToggleGroup(connectionRadioGroup);
        socket.setUserData("socket");
        socket.setSelected(true);
        connectionType = Client.ConnectionType.SOCKET;

        address.textProperty().addListener((observable, oldValue, newValue) -> {
            if(PATTERN.matcher(newValue).matches()||newValue.trim().equals("localhost")){
                addressOk = true;
                address.setStyle(BLACK_TEXT);
            } else {
                addressOk = false;
                address.setStyle(RED_TEXT);
            }
            connect.setDisable(!(addressOk && portOk)|| isConnecting);
        });

        port.textProperty().addListener((observable, oldValue, newValue) -> {
            int portNumber;
            try{
                portNumber = Integer.parseInt(newValue);
                if(portNumber >= 10000 && portNumber <= 65535){
                    portOk = true;
                    port.setStyle(BLACK_TEXT);
                } else {
                    portOk = false;
                    port.setStyle(RED_TEXT);
                }
            } catch (NumberFormatException e){
                portOk = false;
                port.setStyle(RED_TEXT);
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
                lastPortOk = portOk;
                portOk = true;
                connect.setDisable(!addressOk|| isConnecting);
            }
        });
    }

    /***
     * Init the Login Scene, setup flags and event listener for text input
     */
    private void initLoginScene(){
        waitingRoom.setVisible(false);
        playerListView.setVisible(false);
        login.setDisable(true);
        nicknameOk = false;
        passwordOk = false;
        isLogging = false;

        nickname.textProperty().addListener((observable, oldValue, newValue) -> {
            nicknameOk = newValue.length() > 0;
            login.setDisable(!(nicknameOk && passwordOk)|| isLogging);
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length()>3){
                passwordOk = true;
                password.setStyle(BLACK_TEXT);
            } else {
                passwordOk = false;
                password.setStyle(RED_TEXT);
            }
            login.setDisable(!(nicknameOk && passwordOk)|| isLogging);
        });
    }

    /***
     * Init the Schema Choice Scene, connect the buttons click to the client message for choosing schemas
     */
    private void initSchemaChoiceScene(){
        choosingSchema = false;

        schema0.setOnAction(e -> {
            disableSchemaChoice(true);
            try {
                client.sendSchemaChoice(0);
            } catch (ServerReconnectedException e1) {
                LOGGER.warning(e1.toString());
            }
        });
        schema1.setOnAction(e -> {
            disableSchemaChoice(true);
            try {
                client.sendSchemaChoice(1);
            } catch (ServerReconnectedException e1) {
                LOGGER.warning(e1.toString());
            }
        });
        schema2.setOnAction(e -> {
            disableSchemaChoice(true);
            try {
                client.sendSchemaChoice(2);
            } catch (ServerReconnectedException e1) {
                LOGGER.warning(e1.toString());
            }
        });
        schema3.setOnAction(e -> {
            disableSchemaChoice(true);
            try {
                client.sendSchemaChoice(3);
            } catch (ServerReconnectedException e1) {
                LOGGER.warning(e1.toString());
            }
        });
    }

    /***
     * Init the Game Scene, binds the different pane of the scene together to make the scene resizible
     */
    private void initGameScene(){
        goalGrid.prefWidthProperty().bind(tabPane.widthProperty());
        otherGrid.prefWidthProperty().bind(tabPane.widthProperty());
        toolGrid.prefWidthProperty().bind(tabPane.widthProperty());
        tabPane.maxHeightProperty().bind(tabPane.widthProperty().multiply(.55));
        draftPool.setSpacing(draftPool.getHeight()*0.1);
        draftPool.spacingProperty().bind(draftPool.heightProperty().multiply(0.1));
        roundTrack.setSpacing(roundTrack.getHeight()*0.1);
        roundTrack.spacingProperty().bind(roundTrack.heightProperty().multiply(0.1));

        gameStarted = true;
    }


    /***
     * In the Schema Choice Scene, enable or disable the buttons to avoid to choose multiple times
     * @param disable true to disable, false to enable
     */
    private void disableSchemaChoice(boolean disable){
        choosingSchema = disable;
        schema0.setDisable(disable);
        schema1.setDisable(disable);
        schema2.setDisable(disable);
        schema3.setDisable(disable);
    }

    /***
     * Populate Schema Choice Scene with data from Client, show extracted goals for the game
     */
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

        schema0.setText(schemas.get(0).getName());
        schema1.setText(schemas.get(1).getName());
        schema2.setText(schemas.get(2).getName());
        schema3.setText(schemas.get(3).getName());

    }

    /***
     * Populate Game Scene with data from Client, show the goals, the toolcards and the schemas
     */
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
        gameScene.add(playerGrid,1,2);

        info.setAlignment(Pos.CENTER);
        info.setStyle("-fx-border-width : 5; -fx-border-color: black; -fx-border-style:solid;");
        info.setText(MessageHandler.get("info-welcome"));

        nameLabel.setText(client.getGameSnapshot().getPlayer().getNickname());
        DicePane dp = new DicePane(new Dice(getColorFromGoal(client.getGameSnapshot().getPlayer().getPrivateGoal())));
        dp.bindDimension(roundTrack.heightProperty());
        topDisplay.getChildren().add(dp);


        otherPlayerGrids = new ArrayList<>();
        otherPlayerLabels = new ArrayList<>();
        otherPlayerTokens = new ArrayList<>();

        int i = 0;
        for(PlayerSnapshot player : client.getGameSnapshot().getOtherPlayers()){
            VBox vbox = (VBox)otherGrid.getChildren().get(i);

            Label name = new Label(player.getNickname() + (player.isSuspended() ? " (S)" : "") );
            otherPlayerLabels.add(name);
            vbox.getChildren().add(name);

            HBox favor = new HBox();
            favor.prefHeightProperty().bind(vbox.widthProperty().divide(6));
            favor.setSpacing(4);
            favor.setAlignment(Pos.CENTER);
            insertFavorToken(favor,0.35,player.getFavorToken());
            otherPlayerTokens.add(favor);
            vbox.getChildren().add(favor);

            SagradaGridPane pGrid = new SagradaGridPane();
            pGrid.initProperty();
            pGrid.setSchema(player.getWindow().getSchema());
            otherPlayerGrids.add(pGrid);
            vbox.getChildren().add(pGrid);
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

    /***
     * Print the favor tokens as white circle with black border in the given HBox
     * @param container HBox in which show the favor token
     * @param proportion Precentage of the desired radius of the tokens with respect to the container (<0.5)
     * @param num Number of token to print
     */
    private void insertFavorToken(HBox container,double proportion, int num){
        for(int j = 0;j<num;j++){
            Circle c = new Circle(container.getHeight()*proportion);
            c.setFill(Paint.valueOf("white"));
            c.setStroke(Paint.valueOf("black"));
            c.radiusProperty().bind(container.heightProperty().multiply(proportion));
            container.getChildren().add(c);
        }
    }

    /***
     * Given the name of the private goal will return the corresponding color
     * @param privateGoal String with the name of the private goal
     * @return Color Enum corresponding as defined in model
     */
    private Color getColorFromGoal(String privateGoal){
        switch (privateGoal){
            case "Shades of Red": return it.polimi.ingsw.model.Color.RED;
            case "Shades of Yellow": return it.polimi.ingsw.model.Color.YELLOW;
            case "Shades of Green": return it.polimi.ingsw.model.Color.GREEN;
            case "Shades of Blue": return it.polimi.ingsw.model.Color.BLUE;
            case "Shades of Purple": return it.polimi.ingsw.model.Color.PURPLE;
            default: return it.polimi.ingsw.model.Color.RED;
        }
    }

    /***
     * Callback function associated with "Connect" button, try to setup a connection in a different thread and show the results
     */
    public void tryConnect() {

        status.textProperty().set("Connecting...");
        connect.setDisable(true);
        isConnecting = true;

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
                        GUIHandler controller = fxmlLoader.getController();
                        controller.passClient(client);
                        controller.passStage(stage);
                        client.setHandler(controller);
                        stage.setScene(new Scene(root));
                        stage.setTitle("Sagrada - Login");
                    } catch (IOException e) {
                        LOGGER.warning(e.toString());
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

    /***
     * Callback function associated with "Login" button, try to login using the data provided
     */
    public void trylogin() {
        status.textProperty().set("Logging In...");
        login.setDisable(true);
        isLogging = true;
        try {
            client.login(nickname.getText(),password.getText());
        } catch (ServerReconnectedException e) {
            LOGGER.warning(e.toString());
        }
    }

    /***
     * Try to use the required toolcard, in the meantime disable the toolcards buttons
     * @param toolNumber 0-based index of the list containing the toolcard in PlayerSnapshot
     */
    private void useToolCard(int toolNumber){
        List<ToolCard> tools = client.getGameSnapshot().getToolCards();

        try {
            client.useToolCard(tools.get(toolNumber).getName());
        } catch (ServerReconnectedException e) {
            LOGGER.warning(e.toString());
        }

        tool1.setDisable(true);
        tool2.setDisable(true);
        tool3.setDisable(true);
        toolCardInUse = toolNumber;
    }

    public Client getClient(){
        return client;
    }

    /***
     * Pass the client between different scenes
     * @param client reference to the Client instance
     */
    private void passClient(Client client){
        this.client = client;
    }

    /***
     * Pass the stage between different scenes
     * @param stage reference to the primary Stage
     */
    private void passStage(Stage stage){
        this.stage = stage;
    }

    /***
     * Pass the schemas between different scenes
     * @param schemas list of Schema objects
     */
    private void passSchemas(List<Schema> schemas){
        this.schemas = schemas;
    }

    /***
     * Call from the server to refresh the waiting room, the wake up update the view on login scene
     */
    @Override
    public void printWaitingRoom() {
        Platform.runLater(() -> {
            playerList.clear();
            client.getGameSnapshot().getOtherPlayers().forEach(nick -> playerList.add(nick.getNickname()));
            playerList.add(client.getGameSnapshot().getPlayer().getNickname());
        });
        wakeUp(true);
    }

    /***
     * Call from the server, on the game start, show the schemas extracted for the player
     * @param gameSnapshot game info available to the player
     * @param schemas list of schemas extracted
     */
    @Override
    public void printSchemaChoice(GameSnapshot gameSnapshot, List<Schema> schemas) {
        Platform.runLater(() -> {
            try {
                URL path = GUIHandler.class.getClassLoader().getResource("gui-views/schema-choice.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(path);
                Parent root = fxmlLoader.load();
                GUIHandler controller = fxmlLoader.getController();
                controller.passClient(client);
                controller.passStage(stage);
                controller.passSchemas(schemas);
                controller.populateSchemaChoice();
                client.setHandler(controller);
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.setTitle("Sagrada - Schema Choice");
            } catch (IOException e) {
                LOGGER.warning(e.toString());
            }

        });
    }

    /***
     * Call from the server to refresh the view of the game
     * @param gameSnapshot game info available to the player
     */
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

                favToken.getChildren().clear();
                insertFavorToken(favToken,.15,client.getGameSnapshot().getPlayer().getFavorToken());
                playerGrid.updateWindow(client.getGameSnapshot().getPlayer().getWindow());

                for(int i=0;i<otherPlayerGrids.size();i++){
                    PlayerSnapshot player = client.getGameSnapshot().getOtherPlayers().get(i);
                    otherPlayerLabels.get(i).setText(player.getNickname()+ (player.isSuspended() ? " (s)" : ""));
                    otherPlayerTokens.get(i).getChildren().clear();
                    insertFavorToken(otherPlayerTokens.get(i),.35,player.getFavorToken());
                    otherPlayerGrids.get(i).updateWindow(player.getWindow());
                }

                List<ToolCard> tools = client.getGameSnapshot().getToolCards();
                tool1.setText(tools.get(0).getName()+ (tools.get(0).getUsed() ? " (2)" : " (1)"));
                tool2.setText(tools.get(1).getName()+ (tools.get(1).getUsed() ? " (2)" : " (1)"));
                tool3.setText(tools.get(2).getName()+ (tools.get(2).getUsed() ? " (2)" : " (1)"));
            });
        }
    }

    /***
     * Call from the server to refresh the menu, basing on the state of the game propose different options
     * @param gameSnapshot game info available to the player
     */
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
                    try {
                       client.verifyEndTurn();
                    } catch (ServerReconnectedException e) {
                        LOGGER.warning(e.toString());
                    }
                } else {
                    info.setText(MessageHandler.get("info-not-turn"));
                }

                if(alertDiceUnplacable){
                    info.setText(MessageHandler.get("info-unplaceable-dice"));
                    alertDiceUnplacable = false;
                }

            });
        }
    }

    /***
     * Notify the usage of a toolcard
     * @param player player that used the toolcard
     * @param toolCard the toolcard used
     */
    @Override
    public void notifyUsedToolCard(String player, String toolCard) {
        toolCardInUse = 0;
    }

    /***
     * Call from the server to notify the end of a game
     * @param scores ordered list of scores associated with players
     */
    @Override
    public void gameOver(List<Score> scores) {
        Platform.runLater(() -> {
            try {
                URL path = GUIHandler.class.getClassLoader().getResource("gui-views/gameover.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(path);
                Parent root = fxmlLoader.load();
                GUIHandler controller = fxmlLoader.getController();
                controller.passClient(client);
                controller.passStage(stage);
                controller.showScores(scores);
                client.setHandler(controller);
                stage.setScene(new Scene(root));
                stage.setTitle("Sagrada - Gameover");
            } catch (IOException e) {
                LOGGER.warning(e.toString());
            }
        });
    }

    /***
     * Show the scores on the Gameover Scene
     * @param scores ordered list of scores associated with players
     */
    private void showScores(List<Score> scores){
        boolean first = true;
        for(Score score : scores){
            Label label;
            if(first){
                label = new Label(score.getPlayer()+" : "+score.getTotalScore() + " WIN!");
                label.setStyle(RED_TEXT);
                label.setFont(new Font(23));
                first = false;
            } else {
                label = new Label(score.getPlayer()+" - "+score.getTotalScore());
                if(score.getPlayer().equals(client.getGameSnapshot().getPlayer().getNickname())){
                    label.setStyle("-fx-text-fill : green;");
                }
                label.setFont(new Font(20));
            }
            scoresBox.getChildren().add(label);
        }
    }

    @Override
    public void notifyServerDisconnected() {

    }

    /**
     * setup the possibility to roll back during the toolcard for the user
     * @param rollback Signal that user has changed his mind and want to roll back
     */
    private void addRollback(boolean rollback){
        backed = false;
        if(rollback){
            rollbackButton = new Button("Rollback Toolcard");
            rollbackButton.setOnAction(event -> {
                backed = true;
                toolBox.getChildren().remove(rollbackButton);
                resetStyles();
                rollbackButton = null;
                synchronized (this) {
                    this.notifyAll();
                }
                printGame(client.getGameSnapshot());
                printMenu(client.getGameSnapshot());
            });
            toolBox.getChildren().add(rollbackButton);
        }
    }

    /**
     * reset styles added with the toolcard object selection
     */
    private void resetStyles(){
        roundTrack.setStyle("");
        draftPool.setStyle("");
        if(tmpStyle!=null) playerGrid.setStyle(tmpStyle);
    }

    /**
     * Put the server request to sleep while getting an answer
     * @param rollback possibility of rollback in the toolcard
     * @throws RollbackException signal that user has changed his mind and want to roll back
     */
    private void waitForAnswer(boolean rollback) throws RollbackException {
        try {
            synchronized (this){
                wait();
            }

        } catch (InterruptedException e) {
            LOGGER.warning(e.toString());
        }
        
        if(rollbackButton!=null){
            Platform.runLater(() -> toolBox.getChildren().remove(rollbackButton));
        }
        if(tmpStyle!=null) tmpStyle = null;

        if(rollback&&backed) throw new RollbackException();
    }

    /**
     * Show to the user an alert, asking for a plus (true) or a minus (false)
     * @param prompt Request string from the server
     * @param rollback Possibility to rollback
     * @return User's choice true for '+' or false for '-'
     * @throws RollbackException signal that user has changed his mind and want to roll back
     */
    @Override
    public boolean askIfPlus(String prompt, boolean rollback) throws RollbackException {
        Platform.runLater(()->{

            addRollback(rollback);

            ButtonType plus = new ButtonType("+");
            ButtonType minus = new ButtonType("-");
            Alert askBool = new Alert(Alert.AlertType.CONFIRMATION, MessageHandler.get(prompt), plus,minus);

            boolean chose = false;
            
            while (!chose){
                askBool.showAndWait();
                chose = true;
                if(askBool.getResult() == plus) askedBool = true;
                else if(askBool.getResult() == minus) askedBool = false;
                else chose = false;
            }
            

            synchronized (this) {
                this.notifyAll();
            }

        });

        waitForAnswer(rollback);

        LOGGER.info("Returning "+askedBool);
        return askedBool;
    }

    /**
     * Highlight the draftpool to the user asking to click and choose a dice
     * @param prompt Request string from the server
     * @param rollback Possibility to rollback
     * @return Dice chose from the draftpool
     * @throws RollbackException signal that user has changed his mind and want to roll back
     */
    @Override
    public Dice askDiceDraftPool(String prompt, boolean rollback) throws RollbackException {

        Platform.runLater(()->{

            addRollback(rollback);

            info.setText(MessageHandler.get(prompt));
            draftPool.setStyle(RED_DASHED_BORD);
            draftPool.getChildren().forEach(child -> child.setOnMouseClicked(event -> {
                DicePane dp = (DicePane) event.getSource();
                askedDraft = dp.getDice();
                removeFromDraftIfNecessary(dp);
                draftPool.setStyle("");
                synchronized (this) {
                    this.notify();
                }
            }));
        });

        waitForAnswer(rollback);

        LOGGER.info("Returning "+askedDraft);
        return askedDraft;
    }

    /***
     * Highlight the roundtrack to the user asking to click and choose a dice
     * @param prompt Request string from the server
     * @param rollback Possibility to rollback
     * @return the index of the dice chose from the roundtrack
     * @throws RollbackException signal that user has changed his mind and want to roll back
     */
    @Override
    public int askDiceRoundTrack(String prompt, boolean rollback) throws RollbackException {
        Platform.runLater(()->{

            addRollback(rollback);

            info.setText(MessageHandler.get(prompt));
            roundTrack.setStyle(RED_DASHED_BORD);
            roundTrack.getChildren().forEach(child -> child.setOnMouseClicked(event -> {
                roundTrack.setStyle("");
                askedNumber = ((DicePane) event.getSource()).getIdx();
                synchronized (this) {
                    this.notify();
                }
            }));
        });

        waitForAnswer(rollback);

        LOGGER.info("Returning "+askedNumber);
        return askedNumber;
    }

    /***
     * Highlight the player window asking to click and choose a position
     * @param prompt Request string from the server
     * @param rollback Possibility to rollback
     * @return Coordinate chose by the player
     * @throws RollbackException signal that user has changed his mind and want to roll back
     */
    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) throws RollbackException {

        Platform.runLater(()->{

            addRollback(rollback);

            info.setText(MessageHandler.get(prompt));
            tmpStyle = playerGrid.getStyle();
            playerGrid.setStyle(tmpStyle + RED_DASHED_BORD);
            playerGrid.getChildren().forEach(child -> {
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

        waitForAnswer(rollback);

        LOGGER.info("Returning "+askedCoordinate);
        return askedCoordinate;
    }

    /**
     * Show to the user an alert, asking for a valid dice number
     * @param prompt Request string from the server
     * @param rollback Possibility to rollback
     * @return Dice value chose by the player
     * @throws RollbackException signal that user has changed his mind and want to roll back
     */
    @Override
    public int askDiceValue(String prompt, boolean rollback) throws RollbackException {

        Platform.runLater(()->{

            addRollback(rollback);

            ButtonType button1 = new ButtonType("1");
            ButtonType button2 = new ButtonType("2");
            ButtonType button3 = new ButtonType("3");
            ButtonType button4 = new ButtonType("4");
            ButtonType button5 = new ButtonType("5");
            ButtonType button6 = new ButtonType("6");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
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

            if(activeDice!=null) activeDice.getLabel().setText(askedNumber+"");

            synchronized (this) {
                this.notifyAll();
            }

        });

        waitForAnswer(rollback);

        LOGGER.info("Returning "+askedNumber);
        return askedNumber;

    }

    /**
     * Show to the user an alert, asking for the number of move that wants to do
     * @param prompt Request string from the server
     * @param rollback Possibility to rollback
     * @return Integer number of move chose by the player
     * @throws RollbackException signal that user has changed his mind and want to roll back
     */
    @Override
    public int askMoveNumber(String prompt, int n, boolean rollback) throws RollbackException {
        Platform.runLater(()->{

            addRollback(rollback);

            List<ButtonType> buttonNumbers = new ArrayList<>();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getButtonTypes().clear();
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
                this.notifyAll();
            }

        });

        waitForAnswer(rollback);

        LOGGER.info("Returning "+askedNumber);
        return askedNumber;
    }

    /**
     * Show and highlights dice received from the server
     * @param dice the dice to show
     */
    @Override
    public void printDice(Dice dice){
        Platform.runLater(() -> {
            DicePane dp = new DicePane(dice);
            activeDice = dp;
            dp.bindDimension(draftPool.heightProperty());
            dp.setStyle(dp.getStyle() + "-fx-border-color:red; -fx-border-style:dashed;");
            draftPool.getChildren().add(dp);
        });
    }

    /***
     * Multipurpose boolean message from the server, basing on the state handle the response differently
     * @param serverResult response of the server: success (true) or fail (false)
     */
    @Override
    public void wakeUp(boolean serverResult) {
        if(isLogging) handleLogin(serverResult);
        if(choosingSchema) handleSchema(serverResult);
        if(toolCardInUse !=0&&!serverResult){
            Platform.runLater(() -> {
                info.setText(MessageHandler.get("info-tool-fail"));
                tool1.setDisable(false);
                tool2.setDisable(false);
                tool3.setDisable(false);
                toolCardInUse = 0;
            });
        }
        if(playerGrid!=null&&playerGrid.isPlacingDice()&&!serverResult){
            Platform.runLater(() -> info.setText(MessageHandler.get("info-dice-bad")));
        }
    }

    /**
     * Signal from server that an unplaceable dice from toolcard was reput in the draft pool
     * @param dice the dice reput in the draft pool
     */
    @Override
    public void alertDiceInDraftPool(Dice dice){
        alertDiceUnplacable = true;
    }

    /**
     * required only for the blocking clihandler, no need for the gui
     */
    @Override
    public void interruptInput(){

    }

    /**
     * Handle login response from server
     * @param serverResult response
     */
    private void handleLogin(boolean serverResult){
        Platform.runLater(() -> {
            if(serverResult){
                setWaitingRoom();
                stage.setOnCloseRequest( event ->
                {
                    try {
                        client.logout();
                    } catch (ServerReconnectedException e) {
                        LOGGER.warning(e.toString());
                    }
                });
            } else {
                status.textProperty().set("Wrong password!");
                login.setDisable(false);
                isLogging = false;
            }
        });
    }

    /**
     * Set the waiting room, after a login or a gameover
     */
    void setWaitingRoom(){
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

    /**
     * Handle schema choice response from the server
     * @param serverResult response
     */
    private void handleSchema(boolean serverResult){
        Platform.runLater(() -> {
            if(serverResult){
                choosingSchema = false;
                try {
                    URL path = GUIHandler.class.getClassLoader().getResource("gui-views/game.fxml");
                    FXMLLoader fxmlLoader = new FXMLLoader(path);
                    Parent root = fxmlLoader.load();
                    GUIHandler controller = fxmlLoader.getController();
                    controller.passClient(client);
                    controller.passStage(stage);
                    controller.populateGame();
                    client.setHandler(controller);
                    stage.setScene(new Scene(root));
                    stage.setTitle("Sagrada - Game");
                } catch (IOException e) {
                    LOGGER.warning(e.toString());
                }
            } else {
                disableSchemaChoice(false);
            }
        });
    }

    /**
     * callback for button to pass
     */
    public void pass() {
        try {
            client.pass();
        } catch (ServerReconnectedException e) {
            LOGGER.warning(e.toString());
        }
    }

    /**
     * Message from the server signalling a reconnection
     * @param toolCard
     */
    @Override
    public void setToolCardNotCompleted(String toolCard){
        Platform.runLater(() -> {
            URL path = GUIHandler.class.getClassLoader().getResource("gui-views/game.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(path);
            Parent root = null;
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                LOGGER.warning(e.toString());
            }
            GUIHandler controller = fxmlLoader.getController();
            controller.passClient(client);
            controller.passStage(stage);
            controller.populateGame();
            client.setHandler(controller);
            stage.setScene(new Scene(root));
            stage.setTitle("Sagrada - Game");
            if(!toolCard.equals("")) {
                new Thread(() -> {
                    try {
                        client.continueToolCard();
                    } catch (ServerReconnectedException e) {
                        LOGGER.info(e.toString());
                    }
                }).start();
            }
        });
    }

    /**
     * Remove an updated dicepane from the draftpool if required by toolcard
     * @param dp DicePane to remove
     */
    private void removeFromDraftIfNecessary(DicePane dp){
        String toolName = client.getGameSnapshot().getToolCards().get(toolCardInUse).getName();
        if(toolName.equals("Pennello per pasta salda")||toolName.equals("Diluente per pasta salda")||toolName.equals("Pinza Sgrossatrice")
                ||toolName.equals("Tampone Diamantato")){
            draftPool.getChildren().remove(dp);
        }
    }

    /**
     * Callback for "new game" button from gameover scene to start a new game, put the player in waiting room
     */
    public void newGame() {
        try {
            client.newGame();
        } catch (ServerReconnectedException e) {
            LOGGER.warning(e.toString());
        }

        try {
            URL path = GUIHandler.class.getClassLoader().getResource("gui-views/login.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(path);
            Parent root = fxmlLoader.load();
            GUIHandler controller = fxmlLoader.getController();
            controller.passClient(client);
            controller.passStage(stage);
            controller.setWaitingRoom();
            controller.printWaitingRoom();
            client.setHandler(controller);

            stage.setScene(new Scene(root));
            stage.setTitle("Sagrada - Waiting Room");
        } catch (IOException e) {
            LOGGER.warning(e.toString());
        }
    }
}

package it.polimi.ingsw.network.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GuiMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL path = GuiMain.class.getClassLoader().getResource("gui-views/connessione.fxml");
        Parent root = FXMLLoader.load(path);
        primaryStage.setTitle("Sagrada - Connessione");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}

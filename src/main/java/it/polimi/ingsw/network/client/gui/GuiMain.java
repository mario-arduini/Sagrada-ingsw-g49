package it.polimi.ingsw.network.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class GuiMain extends Application {

    private static final String DEF_COLOR = "#f8f6f7";


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

    public static Image getGoalImage(String goal){
        String fileName;
        switch (goal){
            case "RowColorVariety": fileName = "0002.jpg"; break;
            case "ColumnColorVariety": fileName = "0003.jpg"; break;
            case "RowShadeVariety": fileName = "0004.jpg"; break;
            case "ColumnShadeVariety": fileName = "0005.jpg"; break;
            case "LightShades": fileName = "0006.jpg"; break;
            case "MediumShades": fileName = "0007.jpg"; break;
            case "DarkShades": fileName = "0008.jpg"; break;
            case "FullShadeVariety": fileName = "0009.jpg"; break;
            case "DiagonalColor": fileName = "0010.jpg"; break;
            case "FullColorVariety": fileName = "0011.jpg"; break;
            case "Shades of Red": fileName = "0013.jpg"; break;
            case "Shades of Yellow": fileName = "0014.jpg"; break;
            case "Shades of Green": fileName = "0015.jpg"; break;
            case "Shades of Blue": fileName = "0016.jpg"; break;
            case "Shades of Purple": fileName = "0017.jpg"; break;
            default: fileName = "0001.jpg"; break;
        }
        URL path = GuiMain.class.getClassLoader().getResource("gui-views/goals/"+fileName);
        Image goalImage = null;
        try{
            goalImage = new Image(path.openStream());
        } catch (IOException e){
            e.printStackTrace();
        }
        return goalImage;
    }

    public static Image getToolImage(String tool){
        String fileName;
        switch (tool){
            case "Pinza Sgrossatrice": fileName = "0002.jpg"; break;
            case "Pennello per Eglomise": fileName = "0003.jpg"; break;
            case "Alesatore per lamina di rame": fileName = "0004.jpg"; break;
            case "Lathekin": fileName = "0005.jpg"; break;
            case "Taglierina Circolare": fileName = "0006.jpg"; break;
            case "Pennello per pasta salda": fileName = "0007.jpg"; break;
            case "Martelletto": fileName = "0008.jpg"; break;
            case "Tenaglia a rotelle": fileName = "0009.jpg"; break;
            case "Riga in sughero": fileName = "0010.jpg"; break;
            case "Tampone Diamantato": fileName = "0011.jpg"; break;
            case "Diluente per pasta salda": fileName = "0012.jpg"; break;
            case "Taglierina Manuale": fileName = "0013.jpg"; break;
            default: fileName = "0001.jpg"; break;
        }
        URL path = GuiMain.class.getClassLoader().getResource("gui-views/toolcards/"+fileName);
        Image goalImage = null;
        try{
            goalImage = new Image(path.openStream());
        } catch (IOException e){
            e.printStackTrace();
        }
        return goalImage;
    }

    public static String getColor(it.polimi.ingsw.model.Color color){
        switch (color){
            case RED: return "#d72427";
            case PURPLE: return "#a84296";
            case YELLOW: return "#f0da0b";
            case GREEN: return "#04ac6e";
            case BLUE: return "#31bbc5";
            default: return DEF_COLOR;
        }
    }
}

package it.polimi.ingsw.network.client.gui;

import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.network.client.model.Color;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.network.client.gui.GuiMain.getColor;

public class SagradaGridPane extends GridPane {

    private static final String DEF_COLOR = "#f8f6f7";
    private GUIHandler controller;

    public void initProperty(){
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(20.0);
        cc.setHalignment(HPos.CENTER);
        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(25.0);
        rc.setValignment(VPos.CENTER);
        this.setHeight(this.getWidth()*0.8);
        this.getColumnConstraints().addAll(cc,cc,cc,cc,cc);
        this.getRowConstraints().addAll(rc,rc,rc,rc);
        this.hgapProperty().bind(this.heightProperty().multiply(0.02));
        this.vgapProperty().bind(this.heightProperty().multiply(0.02));
        this.maxHeightProperty().bind(this.widthProperty().multiply(0.8));
        this.prefHeightProperty().bind(this.maxHeightProperty());

    }

    public void passController(GUIHandler controller){
        this.controller = controller;
    }

    public void setSchema(Schema schema){
        Constraint constraint;
        this.setStyle("-fx-background-color: #05040c;");
        this.paddingProperty().bind(Bindings.createObjectBinding(() -> new Insets(this.hgapProperty().doubleValue()), this.hgapProperty()));
        for(int r = 0; r< Window.ROW; r++)
            for (int c = 0;c<Window.COLUMN;c++){
                CellPane cell = new CellPane(r,c,this);
                cell.setDiceDroppable();
                constraint = schema.getConstraint(r,c);
                if(constraint!=null){
                    if(constraint.getColor()!=null){
                        cell.setStyle("-fx-background-color: "+getColor(constraint.getColor())+";");
                    } else {
                        cell.setStyle("-fx-background-color: "+DEF_COLOR+";");
                        Text valueLabel = new Text(constraint.getNumber().toString());
                        valueLabel.scaleXProperty().bind(this.widthProperty().multiply(0.01));
                        valueLabel.scaleYProperty().bind(valueLabel.scaleXProperty());
                        cell.getChildren().add(valueLabel);
                    }
                } else {
                    cell.setStyle("-fx-background-color: "+DEF_COLOR+";");
                }
                this.add(cell,c,r);
            }
    }

    public void tryDice(CellPane cellToUpdate,int idx){
        System.out.println("setting dice in : "+cellToUpdate.getRow()+":"+cellToUpdate.getCol());
        if(controller!=null) this.controller.getClient().placeDice(idx,cellToUpdate.getRow()+1,cellToUpdate.getCol()+1);
    }

    CellPane getCell(int r,int c){
        return (CellPane) getChildren().stream().filter(cell -> GridPane.getRowIndex(cell)==r&&GridPane.getColumnIndex(cell)==c).findFirst().get();
    }

    public void updateWindow(Window window){
        for(int r = 0;r<Window.ROW;r++)
            for(int c=0;c<Window.COLUMN;c++){
                Dice dice = window.getCell(r,c);
                CellPane cell = getCell(r,c);
                if(cell.getDice()!=null){
                    cell.getChildren().remove(cell.getDice());
                    cell.setDice(null);
                }
                if(dice!=null){
                    DicePane dp = new DicePane(dice);
                    dp.bindDimension(cell.heightProperty());
                    cell.setDice(dp);
                    cell.getChildren().add(dp);
                }
            }
    }

}

class CellPane extends StackPane{

    private int row;
    private int col;
    private DicePane dice;
    private SagradaGridPane parent;
    private String tmpStyle;

    public CellPane(int row,int col,SagradaGridPane parent){
        super();
        this.row = row;
        this.col = col;
        this.dice = null;
        this.parent = parent;
    }

    public DicePane getDice() {
        return dice;
    }

    public void setDice(DicePane dp){
        this.dice = dp;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void setDiceDroppable(){
        this.setOnDragOver(event -> {
            if (event.getGestureSource() != this  && this.getDice() == null) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        this.setOnDragEntered(event -> {
            this.tmpStyle = this.getStyle();
            this.setStyle(tmpStyle+"-fx-border-color: green; -fx-border-width:2; -fx-border-style: dashed;");
        });

        this.setOnDragExited(event -> {
            this.setStyle(tmpStyle);
        });

        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            int idx = Integer.parseInt(db.getString());
            this.parent.tryDice(this,idx);


            event.setDropCompleted(true);

            event.consume();
        });
    }
}
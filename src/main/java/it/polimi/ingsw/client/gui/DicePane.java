package it.polimi.ingsw.client.gui;

import it.polimi.ingsw.model.Dice;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

/**
 * Class representing a Dice in the GUI
 */
 class DicePane extends StackPane {

    private Label valueLabel;
    private Dice dice;
    private Integer idx;

    /**
     * Construct the GUI element from the given Dice
     * @param dice Dice to represent
     */
    DicePane(Dice dice){
        super();
        if(dice.getColor()!=null)this.setStyle("-fx-border-color: black; -fx-border-width: 3; -fx-border-style: solid; -fx-border-radius:15; -fx-background-radius:15; -fx-background-color: "+GuiMain.getColor(dice.getColor())+";");
        this.dice = dice;
        if(dice.getValue()!=0){
            this.valueLabel = new Label(dice.getValue().toString());
            this.valueLabel.setStyle("-fx-text-fill: white;");
            this.getChildren().add(valueLabel);
        }
        this.idx = 0;
    }

    /**
     * Construct the GUI element from the given Dice and assign the given index
     * @param dice Dice to represent
     * @param idx indec
     */
    DicePane(Dice dice,int idx){
        super();
        if(dice.getColor()!=null)this.setStyle("-fx-border-color: black; -fx-border-width: 3; -fx-border-style: solid; -fx-border-radius:15; -fx-background-radius:15; -fx-background-color: "+GuiMain.getColor(dice.getColor())+";");
        this.dice = dice;
        if(dice.getValue()!=0){
            this.valueLabel = new Label(dice.getValue().toString());
            this.valueLabel.setStyle("-fx-text-fill: white;");
            this.getChildren().add(valueLabel);
        }
        this.idx = idx;
    }

    /**
     * Make dice resizible, binding its dimensions to the Height property of the parent
     * @param par Expected Height Property of the parent
     */
    void bindDimension(ReadOnlyDoubleProperty par){
        this.setMaxHeight(par.getValue()*.8);
        this.setMaxWidth(this.getMaxHeight());
        this.setHeight(this.getMaxHeight());
        this.setWidth(this.getHeight());
        this.maxHeightProperty().bind(par.multiply(0.8));
        this.maxWidthProperty().bind(this.maxHeightProperty());
        this.prefHeightProperty().bind(this.maxHeightProperty());
        this.prefWidthProperty().bind(this.prefHeightProperty());
        if(valueLabel!=null){
            valueLabel.scaleXProperty().bind(Bindings.min(this.heightProperty(),this.widthProperty()).multiply(.04));
            valueLabel.scaleYProperty().bind(valueLabel.scaleXProperty());
        }
    }

    /**
     * Allow the drag and drop operation for the DicePane
     */
    void setDraggable(){
        this.setOnDragDetected(event -> {
            Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent diceData = new ClipboardContent();
            diceData.putString(this.idx.toString());

            db.setContent(diceData);

            event.consume();
        });
    }

    /**
     * Get index of the DicePane
     * @return index of DicePane
     */
    int getIdx(){
        return idx;
    }

    /**
     * Get the Dice represented by the DicePane
     * @return the Dice represented
     */
    Dice getDice(){
        return dice;
    }

    /**
     * Get the Label containing the value of the Dice
     * @return the Label element containing the value of the Dice
     */
    Label getLabel(){
        return valueLabel;
    }
}

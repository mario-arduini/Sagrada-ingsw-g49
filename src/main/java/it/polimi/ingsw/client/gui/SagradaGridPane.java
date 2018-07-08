package it.polimi.ingsw.client.gui;

import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.client.exception.ServerReconnectedException;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import static it.polimi.ingsw.client.gui.GuiMain.getColor;

/**
 * Class representing the Window of a Player in the GUI
 */
public class SagradaGridPane extends GridPane {

    private static final String DEF_COLOR = "#f8f6f7";
    private GUIHandler controller;
    private boolean placingDice;

    /**
     * Init the property of the object
     */
    void initProperty(){
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

    /**
     * Pass the controller of the GUI
     * @param controller controller of the GUI
     */
    void passController(GUIHandler controller){
        this.controller = controller;
    }

    /**
     * Set the Schema associated with the Window
     * @param schema Schema of the Window
     */
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

    /**
     * Try to place the dice on the GUI
     * @param cellToUpdate CellPane that called the function
     * @param idx index of the dice
     */
    void tryDice(CellPane cellToUpdate,int idx){
        placingDice = true;
        if(controller!=null) {
            try {
                this.controller.getClient().placeDice(idx,cellToUpdate.getRow()+1,cellToUpdate.getCol()+1);
            } catch (ServerReconnectedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return true if we are placing a dice, false otherwise
     */
    boolean isPlacingDice() {
        return placingDice;
    }

    /**
     * Set the value of the flag placing dice
     * @param bool value to set
     */
    public void setPlacingDice(boolean bool){
        this.placingDice = bool;
    }

    /**
     * Get the cell by row and column
     * @param r row of the cell
     * @param c column of the cell
     * @return Corresponding cellpane
     */
    private CellPane getCell(int r, int c){
        return (CellPane) getChildren().stream().filter(cell -> GridPane.getRowIndex(cell)==r&&GridPane.getColumnIndex(cell)==c).findFirst().get();
    }

    /**
     * Update the GUI view basing on the corresponding Window
     * @param window
     */
    void updateWindow(Window window){
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

/**
 * Inner class to represent a Cell of the Window in the GUI
 */
class CellPane extends StackPane{

    private int row;
    private int col;
    private DicePane dice;
    private SagradaGridPane parent;
    private String tmpStyle;

    /**
     * Pass row, column and parent
     * @param row row of the cell
     * @param col col of the cell
     * @param parent parent window
     */
    CellPane(int row,int col,SagradaGridPane parent){
        super();
        this.row = row;
        this.col = col;
        this.dice = null;
        this.parent = parent;
    }

    /**
     * Get the Dice of the cell or null
     * @return Dice stored in the cell or null
     */
    public DicePane getDice() {
        return dice;
    }

    /**
     * Set the DicePane of the Cell
     * @param dp
     */
    public void setDice(DicePane dp){
        this.dice = dp;
    }

    /**
     * @return column of the cell
     */
    public int getCol() {
        return col;
    }

    /**
     * @return row of the cell
     */
    public int getRow() {
        return row;
    }

    /**
     * Make the cell a dropping place, for drag and drop operation with DicePane
     */
    void setDiceDroppable(){
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

        this.setOnDragExited(event -> this.setStyle(tmpStyle));

        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            int idx = Integer.parseInt(db.getString());
            this.parent.tryDice(this,idx);


            event.setDropCompleted(true);

            event.consume();
        });
    }
}
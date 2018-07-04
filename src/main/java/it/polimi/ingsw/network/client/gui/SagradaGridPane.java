package it.polimi.ingsw.network.client.gui;

import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.network.client.model.Color;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.function.DoublePredicate;

public class SagradaGridPane extends GridPane {

    private static final String DEF_COLOR = "#f8f6f7";

    public void initProperty(){
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(20.0);
        cc.setHalignment(HPos.CENTER);
        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(25.0);
        rc.setValignment(VPos.CENTER);
        this.getColumnConstraints().addAll(cc,cc,cc,cc,cc);
        this.getRowConstraints().addAll(rc,rc,rc,rc);
        this.hgapProperty().bind(this.heightProperty().multiply(0.02));
        this.vgapProperty().bind(this.heightProperty().multiply(0.02));
        this.maxHeightProperty().bind(this.widthProperty().multiply(0.8));
    }

    public void setSchema(Schema schema){
        Constraint constraint;
        VBox cell;
        this.setStyle("-fx-background-color: #05040c;");
        this.paddingProperty().bind(Bindings.createObjectBinding(() -> new Insets(this.hgapProperty().doubleValue()), this.hgapProperty()));
        for(int r = 0; r< Window.ROW; r++)
            for (int c = 0;c<Window.COLUMN;c++){
                cell = new VBox();
                constraint = schema.getConstraint(r,c);
                if(constraint!=null){
                    if(constraint.getColor()!=null){
                        cell.setStyle("-fx-background-color: "+getColor(constraint.getColor())+";");
                        this.add(cell,c,r);
                    } else {
                        cell.setAlignment(Pos.CENTER);
                        cell.setStyle("-fx-background-color: "+DEF_COLOR+";");
                        Label valueLabel = new Label(constraint.getNumber().toString());
                        valueLabel.scaleXProperty().bind(this.widthProperty().multiply(0.01));
                        valueLabel.scaleYProperty().bind(valueLabel.scaleXProperty());
                        cell.getChildren().add(valueLabel);
                        this.add(cell,c,r);
                    }
                } else {
                    cell.setStyle("-fx-background-color: "+DEF_COLOR+";");
                    this.add(cell,c,r);
                }
            }

    }

    private String getColor(it.polimi.ingsw.model.Color color){
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

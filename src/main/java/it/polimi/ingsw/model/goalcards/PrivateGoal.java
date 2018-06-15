package it.polimi.ingsw.model.goalcards;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrivateGoal implements Goal{
    private Color color;

    public PrivateGoal(Color color){
        this.color = color;
    }

    public String getName() {
        return "Shades of " + color.toString();
    }

    public int computeScore(Window window) {
        List<Dice> mosaic = new ArrayList<Dice>();
        for(int i=0;i<Window.ROW;i++)
            for(int j=0;j<Window.COLUMN;j++)
                mosaic.add( window.getCell(i,j) );

        return mosaic.stream().filter(Objects::nonNull)
                .filter(dice -> dice.getColor() == this.color)
                .map(dice -> dice.getValue())
                .reduce(0, (sum,val) -> sum+val);
    }
}

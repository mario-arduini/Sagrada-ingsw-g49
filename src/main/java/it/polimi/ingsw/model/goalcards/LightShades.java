package it.polimi.ingsw.model.goalcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LightShades extends PublicGoal {

    @Override
    public int computeScore(Window window) {
        List<Dice> mosaic = new ArrayList<Dice>();
        for(int i=0;i<Window.ROW;i++)
            for(int j=0;j<Window.COLUMN;j++)
                mosaic.add( window.getCell(i,j) );

        int coded = mosaic.stream().filter(Objects::nonNull)
                .map(dice -> dice.getValue())
                .map(val -> { if(val==1) return 1;
                              if(val==2) return 64;
                              return 0;})
                .reduce(0, (sum,val) -> sum+val);
        return Math.min(coded%64,coded>>6) * 2;

    }
}

package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FullColorVariety extends PublicGoal {
    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int computeScore(Window window) {
        int count[] = new int[5];
        for(int i=0;i<Window.ROW;i++)
            for(int j=0;j<Window.COLUMN;j++)
                if(window.getCell(i,j)!= null) switch (window.getCell(i,j).getColor()){
                    case RED: count[0]++; break;
                    case GREEN: count[1]++; break;
                    case BLUE: count[2]++; break;
                    case PURPLE: count[3]++; break;
                    case YELLOW: count[4]++; break;
                }
        int numberOfSet = count[0];
        for(int x : count)
            numberOfSet = Math.min(x,numberOfSet);
        return numberOfSet*4;
    }
}

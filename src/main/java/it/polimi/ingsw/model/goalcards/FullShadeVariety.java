package it.polimi.ingsw.model.goalcards;

import it.polimi.ingsw.model.Window;

public class FullShadeVariety extends PublicGoal {

    @Override
    public int computeScore(Window window) {
        int count[] = new int[6];
        for(int i=0;i<Window.ROW;i++)
            for(int j=0;j<Window.COLUMN;j++)
                if(window.getCell(i,j)!= null)
                    count[window.getCell(i,j).getValue()-1]++;
        int numberOfSet = count[0];
        for(int x : count)
            numberOfSet = Math.min(x,numberOfSet);
        return numberOfSet*5;
    }
}

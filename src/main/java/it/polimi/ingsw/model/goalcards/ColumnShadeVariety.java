package it.polimi.ingsw.model.goalcards;

import it.polimi.ingsw.model.Window;

public class ColumnShadeVariety extends PublicGoal {

    public String getName() {
        return this.getClass().getName();
    }

    public int computeScore(Window window) {
        int score=0;
        boolean flag;
        for(int i=0;i<Window.COLUMN;i++){
            flag = true;
            for(int j=1;j<Window.ROW&&flag;j++)
                for(int k=0;k<j;k++) {
                    if (window.getCell(j,i)==null||window.getCell(k,i)==null||window.getCell(j,i).getValue() == window.getCell(k,i).getValue()) {
                        flag = false;
                    }
                }
            if(flag) score += 4;
        }
        return score;
    }
}

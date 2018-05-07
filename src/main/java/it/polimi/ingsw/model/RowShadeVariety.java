package it.polimi.ingsw.model;

public class RowShadeVariety extends PublicGoal {

    public String getName() {
        return this.getClass().getName();
    }

    public int computeScore(Window window) {
        int score=0;
        boolean flag;
        for(int i=0;i<Window.ROW;i++){
            flag = true;
            for(int j=1;j<Window.COLUMN&&flag;j++)
                for(int k=0;k<j;k++) {
                    if (window.getCell(i,j)==null||window.getCell(i,k)==null||window.getCell(i, j).getValue() == window.getCell(i, k).getValue()) {
                        flag = false;
                    }
                }
            if(flag) score += 5;
        }
        return score;
    }
}

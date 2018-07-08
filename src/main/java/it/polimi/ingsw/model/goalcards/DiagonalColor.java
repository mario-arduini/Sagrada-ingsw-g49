package it.polimi.ingsw.model.goalcards;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Window;

public class DiagonalColor extends PublicGoal {

    @Override
    public int computeScore(Window window) {
        boolean[][] check = new boolean[Window.ROW][Window.COLUMN];
        int score = 0;
        for(int i=0;i<Window.ROW;i++) {
            for(int j = 0; j<Window.COLUMN; j++) {
                if (window.getCell(i, j) != null) {
                    Color current = window.getCell(i, j).getColor();
                    if (i > 0 && j > 0 && window.getCell(i - 1, j - 1) != null
                            && window.getCell(i - 1, j - 1).getColor() == current) {
                        check[i][j] = true;
                        check[i - 1][j - 1] = true;
                    }
                    if (i < Window.ROW - 1 && j > 0 && window.getCell(i + 1, j - 1) != null
                            && window.getCell(i + 1, j - 1).getColor() == current) {
                        check[i][j] = true;
                        check[i + 1][j - 1] = true;
                    }
                }
            }
        }
        for(int i=0;i<Window.ROW;i++) for(int j=0;j<Window.COLUMN;j++) if(check[i][j]) score++;
        return score;
    }
}

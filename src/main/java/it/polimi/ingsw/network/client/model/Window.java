package it.polimi.ingsw.network.client.model;

public class Window {
    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private final Schema schema;
    private Dice[][] mosaic;

    public Window(Window window){
        this.schema = window.schema;
        this.mosaic = new Dice[ROW][COLUMN];

        for(int i = 0; i < ROW; i++)
            for(int j = 0; j < COLUMN; j++)
                if(window.getCell(i, j) != null)
                    this.mosaic[i][j] = new Dice(window.getCell(i, j));
    }

    public Window(Schema schema){
        this.schema = schema;
        this.mosaic = new Dice[ROW][COLUMN];
    }

    public Schema getSchema(){
        return this.schema;
    }

    public Dice getCell(int row, int column){
        return mosaic[row][column];
    }

    public void removeDice(int row,int column){
        mosaic[row][column] = null;
    }

    public void addDice(int row, int column, Dice dice) {
         this.mosaic[row][column] = new Dice(dice);
    }

    @Override
    public boolean equals(Object window) {

        if(!(window instanceof Window))
            return false;
        for(int i = 0; i < ROW; i++)
            for (int j = 0; j < COLUMN; j++){
                if ((this.mosaic[i][j] != null && ((Window) window).getCell(i, j) == null) || (this.mosaic[i][j] == null && ((Window) window).getCell(i, j) != null))
                    return false;
                if (this.mosaic[i][j] != null && ((Window) window).getCell(i, j) != null && !this.mosaic[i][j].equals(((Window) window).getCell(i, j)))
                    return false;
        }

        return this.schema.equals(((Window)window).getSchema());
    }
}

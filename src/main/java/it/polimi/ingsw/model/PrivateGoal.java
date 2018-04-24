package it.polimi.ingsw.model;

public class PrivateGoal implements Goal{
    private Color color;

    public PrivateGoal(Color color){
        this.color = color;
    }

    public String getName() {
        return "ShadesOf" + color.toString();       //control the use of toString()
    }

    public int computeScore(Window window) {    //not implemented yet
        return 0;
    }
}

package it.polimi.ingsw.model;

public enum Color{

    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m");

    private String escape;
    public static final String RESET = "\u001B[0m";

    Color(String escape) {
        this.escape = escape;
    }

    public String escape() {
        return escape;
    }

    @Override
    public String toString(){
        switch (this){
            case RED: return "Red";
            case GREEN: return "Green";
            case YELLOW: return "Yellow";
            case BLUE: return "Blue";
            case PURPLE: return "Purple";
            default: return "";
        }
    }
}
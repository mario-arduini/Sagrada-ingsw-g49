package it.polimi.ingsw.model;

/**
 * Enum representing the possible colors used in the game
 */
public enum Color{

    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m");

    public static final String RESET = "\u001B[0m";
    private String escape;

    Color(String escape) {
        this.escape = escape;
    }

    /**
     * return unicode representation needed to change color
     * @return Unicode String
     */
    public String escape() {
        return escape;
    }

    /**
     * get the name of the color
     * @return String with name of the color
     */
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
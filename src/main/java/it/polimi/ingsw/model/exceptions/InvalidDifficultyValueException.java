package it.polimi.ingsw.model.exceptions;

public class InvalidDifficultyValueException extends Exception {
    public InvalidDifficultyValueException() {
        super();
    }

    public InvalidDifficultyValueException(String s){
        super(s);
    }
}

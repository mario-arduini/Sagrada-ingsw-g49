package it.polimi.ingsw.network.client.model.exception;

public class InvalidDifficultyValueException extends Exception {
    public InvalidDifficultyValueException() {
        super();
    }

    public InvalidDifficultyValueException(String s){
        super(s);
    }
}

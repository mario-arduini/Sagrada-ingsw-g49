package it.polimi.ingsw.model.exceptions;

public class PrivateGoalAlreadySetException extends Exception {
    public PrivateGoalAlreadySetException() {
        super();
    }

    public PrivateGoalAlreadySetException(String s){
        super(s);
    }
}

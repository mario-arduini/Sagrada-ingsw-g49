package it.polimi.ingsw.model.exceptions;

public class WindowAlreadySetException extends Exception {
    public WindowAlreadySetException() {
        super();
    }

    public WindowAlreadySetException(String s){
        super(s);
    }
}

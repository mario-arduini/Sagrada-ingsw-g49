package it.polimi.ingsw.model.exceptions;

public class NoDiceInWindowException extends Exception {
    public NoDiceInWindowException() {
        super();
    }

    public NoDiceInWindowException(String s){
        super(s);
    }
}

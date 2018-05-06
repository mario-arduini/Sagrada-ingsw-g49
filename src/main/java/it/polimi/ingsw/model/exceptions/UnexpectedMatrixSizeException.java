package it.polimi.ingsw.model.exceptions;

public class UnexpectedMatrixSizeException extends Exception {
    public UnexpectedMatrixSizeException() {
        super();
    }

    public UnexpectedMatrixSizeException(String s){
        super(s);
    }
}

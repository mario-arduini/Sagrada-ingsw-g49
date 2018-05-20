package it.polimi.ingsw.socket;


public class Logger {
    public static synchronized void print(Object o){
        System.out.println(o);
    }
}

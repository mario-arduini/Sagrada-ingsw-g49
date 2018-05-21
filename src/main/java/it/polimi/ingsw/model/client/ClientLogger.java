package it.polimi.ingsw.model.client;

class ClientLogger {
    static synchronized void print(Object object){
        System.out.print(object);
    }
    static synchronized void println(Object object){
        System.out.println(object);
    }
}

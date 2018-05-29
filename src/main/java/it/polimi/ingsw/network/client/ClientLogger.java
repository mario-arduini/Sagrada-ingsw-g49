package it.polimi.ingsw.network.client;

class ClientLogger {
    static synchronized void print(Object object){
        System.out.print(object);
        System.out.flush();
    }
    static synchronized void println(Object object){
        System.out.println(object);
        System.out.flush();
    }
}

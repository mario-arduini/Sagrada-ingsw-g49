package it.polimi.ingsw.socket;


import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
    public static synchronized void print(Object o){
        System.out.print(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) + " ");
        System.out.println(o);
    }
}

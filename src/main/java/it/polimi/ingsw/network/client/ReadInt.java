package it.polimi.ingsw.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;

public class ReadInt implements Callable<String> {

    private BufferedReader input;

    ReadInt(BufferedReader input){
        this.input = input;
    }

    public String call() throws IOException{
        return input.readLine();
    }
}
package it.polimi.ingsw.network.client;

import java.io.Serializable;
import java.rmi.Remote;

public interface ClientRMIInterface{
    void salute(String message);
}

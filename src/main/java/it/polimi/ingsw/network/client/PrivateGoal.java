package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.Color;

public class PrivateGoal{
    private Color color;

    public PrivateGoal(Color color){
        this.color = color;
    }

    public String getName() {
        return "ShadesOf" + color.toString();
    }
}

package it.polimi.ingsw;

import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Color;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Window window = new Window(null);
        System.out.println(window.addDice(1,1, new Dice(Color.RED, 3)));
    }
}


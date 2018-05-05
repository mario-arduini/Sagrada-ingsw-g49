package it.polimi.ingsw;

import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.exceptions.DiceViolatesConstraintException;
import it.polimi.ingsw.model.exceptions.NoSuchWindowCellException;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Window window = new Window(null);
        try {
            window.addDice(1,1, new Dice(Color.RED, 3));
        } catch (NoSuchWindowCellException e) {
            e.printStackTrace();
        } catch (DiceViolatesConstraintException e) {
            e.printStackTrace();
        }
    }
}


package it.polimi.ingsw;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Factory;
import it.polimi.ingsw.model.PrivateGoal;
import it.polimi.ingsw.model.ToolCard;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import it.polimi.ingsw.model.exceptions.OutOfCardsException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FactoryTest {

    @Test
    void extractToolTest(){
        ArrayList<ToolCard>  tools = new ArrayList<ToolCard>();
        ToolCard current = null;
        Factory factory = new Factory();
        for (int i=0;i<12;i++){
            try {
                current = factory.extractToolCard();
            } catch (OutOfCardsException e) {
                e.printStackTrace();
            }
            for(ToolCard t : tools) assertNotEquals(t.getName(),current.getName());
        }
        assertThrows(OutOfCardsException.class,()->{ factory.extractToolCard(); });


    }

    @Test
    void extractPrivateTest(){
        ArrayList<PrivateGoal>  priv = new ArrayList<PrivateGoal>();
        PrivateGoal current = null;
        Factory factory = new Factory();
        for (int i=0;i<5;i++){
            try {
                current = factory.extractPrivateGoal();
            } catch (OutOfCardsException e) {
                e.printStackTrace();
            }
            for(PrivateGoal t : priv) assertNotEquals(t.getName(),current.getName());
        }
        assertThrows(OutOfCardsException.class,()->{ factory.extractPrivateGoal(); });


    }
}
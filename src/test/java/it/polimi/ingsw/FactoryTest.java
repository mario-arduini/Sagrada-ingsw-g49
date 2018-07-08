package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.OutOfCardsException;
import it.polimi.ingsw.model.goalcards.PrivateGoal;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {

    @Test
    void extractToolTest(){
        ArrayList<ToolCard>  tools = new ArrayList<>();
        ToolCard current = null;
        Factory factory = new Factory();
        for (int i=0;i<12;i++){
            try {
                current = factory.extractToolCard();
            } catch (OutOfCardsException e) {
                e.printStackTrace();
                fail();
            }
            for(ToolCard t : tools) assertNotEquals(t.getName(),current.getName());
        }
        assertThrows(OutOfCardsException.class, factory::extractToolCard);
    }

    @Test
    void extractPrivateTest(){
        ArrayList<PrivateGoal>  priv = new ArrayList<>();
        PrivateGoal current = null;
        Factory factory = new Factory();
        for (int i=0;i<5;i++){
            try {
                current = factory.extractPrivateGoal();
            } catch (OutOfCardsException e) {
                e.printStackTrace();
                fail();
            }
            for(PrivateGoal t : priv) assertNotEquals(t.getName(),current.getName());
        }
        assertThrows(OutOfCardsException.class, factory::extractPrivateGoal);
    }

    @Test
    void extractPublicTest(){
        ArrayList<PublicGoal>  pub = new ArrayList<>();
        PublicGoal current = null;
        Factory factory = new Factory();
        for (int i=0;i<10;i++){
            try {
                current = factory.extractPublicGoal();
            } catch (OutOfCardsException e) {
                e.printStackTrace();
                fail();
            }
            for(PublicGoal t : pub) assertNotEquals(t.getName(),current.getName());
        }
        assertThrows(OutOfCardsException.class, factory::extractPublicGoal);
    }
}
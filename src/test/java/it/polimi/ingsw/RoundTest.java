package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class RoundTest {

    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private static final int MIN_DIFFICULTY = 3;

    @Test
    void roundTest() {

        Schema schema;
        Constraint[][] constraint;
        Round round;
        Player p1 = null;
        Player p2 = null;
        Player p3 = null;
        Player p4 = null;
        List<Player> players = new ArrayList<Player>();
        List<Player> players2 = new ArrayList<Player>();
        List<Dice> draftPool = new ArrayList<Dice>();

        try {
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint);

            p1 = new Player("Marco", "jksdkjsd");
            p2 = new Player("Luca", "jksd123?kjsd");
            p3 = new Player("Matteo", "jksd12323kjsd");
            p4 = new Player("Maria", "9032wsdj");
            p1.setWindow(schema);

            players.add(p1);
            players.add(p2);
            players.add(p3);
            players.add(p4);
        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

        Dice d1 = null;
        try {
            d1 = new Dice(Color.RED, 2);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }
        draftPool.add(d1);

        for(int i = 0; i < 4; i++) {
            try {
                draftPool.add(new Dice(Color.RED, (new Random()).nextInt(6) + 1));
            } catch (InvalidDiceValueException e) {
                e.printStackTrace();
            }
        }
        players2.add(p1);
        players2.add(p2);


        round = new Round(draftPool, players);
        assertEquals(round, new Round(draftPool, players));
        assertNotEquals(round, new Round(draftPool, players2));
        assertEquals(round, new Round(round));
        assertArrayEquals(draftPool.toArray(), round.getDraftPool().toArray());
        assertFalse(round.isLastTurn());

        try {
            assertEquals(p1, round.nextPlayer());
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        try {
            round.useDice(0, 0, new Dice(Color.GREEN));
        } catch (DiceNotInDraftPoolException e) {
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }

        try {
            round.useDice(0, 0, d1);
        } catch (Exception e) {
            assertTrue(false);
        }

        try {
            round.useDice(0, 0, d1);
        } catch (DiceAlreadyExtractedException e) {
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }

        draftPool.remove(d1);
        assertArrayEquals(draftPool.toArray(), round.getDraftPool().toArray());

        p2.suspend();
        p3.suspend();
        try {
            assertEquals(p4, round.nextPlayer());
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }
        assertTrue(round.isLastTurn());

        round.suspendPlayer();
        assertTrue(p4.isSuspended());

        try {
            round.nextPlayer();
            assertTrue(false);

        } catch (NoMorePlayersException e) {
            assertTrue(true);
        }
    }
}

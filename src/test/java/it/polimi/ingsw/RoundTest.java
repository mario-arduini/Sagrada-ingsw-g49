package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class RoundTest {

    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private static final int MIN_DIFFICULTY = 3;

    @Test
    void roundTest() {

        AtomicReference<Schema> schema = new AtomicReference<>();
        Constraint[][] constraint;
        Round round;
        Round round1;
        AtomicReference<Player> player = new AtomicReference<>();
        Player player1 = new Player("Marco", "jksdkjsd");
        Player player2 = new Player("Luca", "jksdice23?kjsd");
        Player player3 = new Player("Matteo", "jksdice2323kjsd");
        Player player4 = new Player("Maria", "9032wsdj");
        Player player5 = new Player("Maria", "9032wsdj");
        List<Player> players = new ArrayList<>();
        List<Player> players2 = new ArrayList<>();
        List<Dice> draftPool = new ArrayList<>();
        List<Dice> draftPool1;
        constraint = new Constraint[ROW][COLUMN];

        assertDoesNotThrow(() ->  schema.getAndSet(new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name)));

        assertDoesNotThrow(() ->  player1.setWindow(schema.get()));
        players.add(player1);
        players.add(player2);
        players.add(player3);
        players.add(player4);
        players.add(player5);

        AtomicReference<Dice> dice = new AtomicReference<>();
        assertDoesNotThrow(() -> dice.getAndSet(new Dice(Color.RED, 2)));

        draftPool.add(dice.get());
        for(int i = 0; i < 4; i++)
            assertDoesNotThrow(() -> draftPool.add(new Dice(Color.RED, (new Random()).nextInt(6) + 1)));

        players2.add(player1);
        players2.add(player2);

        round = new Round(draftPool, players);
        assertEquals(round, new Round(draftPool, players));
        assertNotEquals(round, new Round(draftPool, players2));
        assertEquals(round, new Round(round));
        assertArrayEquals(draftPool.toArray(), round.getDraftPool().toArray());
        assertFalse(round.isLastTurn());

        assertDoesNotThrow(() -> player.getAndSet(round.nextPlayer()));
        assertEquals(player1, player.get());

        assertThrows(DiceNotInDraftPoolException.class, () -> round.useDice(0, 0, new Dice(Color.GREEN)));
        assertDoesNotThrow(() -> round.useDice(0, 0, dice.get()));
        assertThrows(DiceAlreadyExtractedException.class, () -> round.useDice(0, 0, dice.get()));

        draftPool.remove(dice.get());
        assertArrayEquals(draftPool.toArray(), round.getDraftPool().toArray());

        player2.suspend();
        player3.suspend();
        assertDoesNotThrow(() -> player.getAndSet(round.nextPlayer()));
        assertEquals(player4, player.get());
        assertDoesNotThrow(() -> player.getAndSet(round.nextPlayer()));
        assertEquals(player5, player.get());

        round.suspendPlayer();
        assertTrue(player5.isSuspended());

        assertDoesNotThrow(() -> player.getAndSet(round.nextPlayer()));
        assertEquals(player4, player.get());
        assertDoesNotThrow(() -> player.getAndSet(round.nextPlayer()));
        assertEquals(player1, player.get());
        assertTrue(round.isLastTurn());

        assertThrows(NoMorePlayersException.class, round::nextPlayer);

        round1 = new Round(round);
        assertEquals(round, round1);

        draftPool1 = new ArrayList<>(draftPool);
        round1 = new Round(draftPool1, players);
        assertEquals(round, round1);

        draftPool1.add(new Dice(Color.RED));
        round1 = new Round(draftPool1, players);
        assertNotEquals(round, round1);

        round1 = new Round(draftPool1, new ArrayList<>());
        assertNotEquals(round, round1);

        assertNotEquals(round, new Object());
    }
}

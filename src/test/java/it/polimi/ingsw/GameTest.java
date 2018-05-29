package it.polimi.ingsw;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    String nick1 = "Daniel";
    String nick2 = "Thanos";

    private List<Player> createPlayers(){
        List<Player> players = new ArrayList<>();

        players.add(new Player(nick1, "bWFsYW5kcmlubwo="));
        players.add(new Player(nick2, "Z3VhbnRvbmUK" ));

        return players;
    }

    @Test
    void testConstructor(){

        AtomicReference<Game> game = new AtomicReference<>();
        List<Player> players;
        players = createPlayers();
        assertDoesNotThrow(() -> game.getAndSet(new Game(players)));


        assertEquals(players, game.get().getPlayers());
        assertEquals(2, game.get().getPlayers().size());
        assertNotEquals(null, game.get().getPublicGoals());
        assertEquals(3,game.get().getPublicGoals().length);
        assertNotEquals(null, game.get().getToolCards());
        assertEquals(3, game.get().getToolCards().length);
        assertFalse(game.get().isGameFinished());
        assertNotEquals(null, game.get().getCurrentRound());
        assertNotEquals(null, game.get().getRoundTrack());
        System.out.println(game.get().getPlayerByNick(nick1));
        assertEquals(players.get(0), game.get().getPlayerByNick(nick1));
        assertEquals(players.get(1), game.get().getPlayerByNick(nick2));
    }

    @Test
    void testNextRound(){
        AtomicReference<Game> game = new AtomicReference<>();
        List<Player> players;
        int index = 0;

        players = createPlayers();
        assertDoesNotThrow(() -> game.getAndSet(new Game(players)));

        //Check first player is inserted in round


        AtomicReference<Player> tmpPlayer = new AtomicReference<>();
        assertDoesNotThrow(() -> tmpPlayer.getAndSet(game.get().getCurrentRound().getCurrentPlayer()));
        assertTrue(players.contains(tmpPlayer.get()));
        index = players.indexOf(tmpPlayer.get());

        game.get().nextRound();

        //Check first player is circular

        assertDoesNotThrow(() -> tmpPlayer.getAndSet(game.get().getCurrentRound().getCurrentPlayer()));
        assertTrue(players.contains(tmpPlayer.get()));
        assertEquals(players.get((index + 1) % players.size()), tmpPlayer.get());


    }

}

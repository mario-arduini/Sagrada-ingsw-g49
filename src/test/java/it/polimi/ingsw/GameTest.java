package it.polimi.ingsw;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

        Game game;
        List<Player> players;

        players = createPlayers();
        game = new Game(players);

        assertEquals(players, game.getPlayers());
        assertEquals(2, game.getPlayers().size());
        assertNotEquals(null, game.getPublicGoals());
        assertEquals(3,game.getPublicGoals().length);
        assertNotEquals(null, game.getToolCards());
        assertEquals(3, game.getToolCards().length);
        assertFalse(game.isGameFinished());
        assertNotEquals(null, game.getCurrentRound());
        assertNotEquals(null, game.getRoundTrack());
        System.out.println(game.getPlayerByNick(nick1));
        assertEquals(players.get(0), game.getPlayerByNick(nick1));
        assertEquals(players.get(1), game.getPlayerByNick(nick2));
    }

    @Test
    void testNextRound(){
        Game game;
        List<Player> players;
        Player tmpPlayer;
        int index = 0;

        players = createPlayers();
        game = new Game(players);

        //Check first player is inserted in round
        try {
            tmpPlayer = game.getCurrentRound().nextPlayer();
            assertTrue(players.contains(tmpPlayer));
            index = players.indexOf(tmpPlayer);
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }
        game.nextRound();

        //Check first player is circular
        try {
            tmpPlayer = game.getCurrentRound().nextPlayer();
            assertTrue(players.contains(tmpPlayer));
            assertEquals(players.get((index + 1) % players.size()), tmpPlayer);
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

    }

}

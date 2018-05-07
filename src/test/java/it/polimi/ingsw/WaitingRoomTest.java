package it.polimi.ingsw;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.WaitingRoom;
import it.polimi.ingsw.model.exceptions.PlayerAlreadyAddedException;
import org.junit.jupiter.api.Test;

import java.util.Timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WaitingRoomTest {
    @Test
    void waitingRoomTest(){

        WaitingRoom waitingRoom = WaitingRoom.getWaitingRoom();
        assertTrue(waitingRoom.getPlayers().size() == 0);
        Player player1 = new Player("Marco", "ahk3jh3244b3");
        Player player2 = new Player("Armando", "b9832782nx");
        Player player3 = new Player("Mario", "sknjeh48!");
        Player player4 = new Player("Marilena", "jsdbnas34");
        Player player5 = new Player("Lucia", "d?fdppab28");

        try {
            waitingRoom.addPlayer(player1);
            waitingRoom.addPlayer(player1);
            assertTrue(false);

        } catch (PlayerAlreadyAddedException e) {
            assertTrue(waitingRoom.getPlayers().size() == 1);
            assertEquals(player1, waitingRoom.getPlayers().get(0));
        }

        try {
            waitingRoom.addPlayer(player2);
            assertTrue(waitingRoom.getPlayers().size() == 2);
            assertEquals(player2, waitingRoom.getPlayers().get(1));
            waitingRoom.removePlayer(player2);
            assertTrue(waitingRoom.getPlayers().size() == 1);
            assertEquals(player1, waitingRoom.getPlayers().get(0));
            waitingRoom.removePlayer(player1);
            assertTrue(waitingRoom.getPlayers().size() == 0);

        } catch (PlayerAlreadyAddedException e) {
            assertTrue(false);
        }

        try {
            waitingRoom.addPlayer(player1);
            waitingRoom.addPlayer(player2);
            assertTrue(waitingRoom.getPlayers().size() == 2);
            assertEquals(player1, waitingRoom.getPlayers().get(0));
            assertEquals(player2, waitingRoom.getPlayers().get(1));
            //assertTrue(waitingRoom.getTimer() > 0);
            waitingRoom.removePlayer(player2);
            //assertTrue(waitingRoom.getTimer() == 0);
            waitingRoom.addPlayer(player2);
            waitingRoom.addPlayer(player3);
            waitingRoom.removePlayer(player3);
            //assertTrue(waitingRoom.getTimer() > 0);
            waitingRoom.addPlayer(player3);
            waitingRoom.addPlayer(player4);
            //assertTrue(waitingRoom.getTimer() == 0); //TODO control game creation
            assertTrue(waitingRoom.getPlayers().size() == 0);

        } catch (PlayerAlreadyAddedException e) {
            assertTrue(false);
        }

        try {
            waitingRoom.addPlayer(player1);
            waitingRoom.addPlayer(player2);
            Timer timer = waitingRoom.getTimer();
            //when timer expires
            //assertTrue(waitingRoom.getTimer() == 0);  //TODO control game creation
            //assertTrue(waitingRoom.getPlayers().size() == 0);
        } catch (PlayerAlreadyAddedException e) {
            assertTrue(false);
        }
    }
}

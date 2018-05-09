package it.polimi.ingsw;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.WaitingRoom;
import it.polimi.ingsw.model.exceptions.PlayerAlreadyAddedException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaitingRoomTest {
    @Test
    void waitingRoomTest(){

        WaitingRoom waitingRoom = WaitingRoom.getWaitingRoom();
        assertEquals(0, waitingRoom.getPlayers().size());
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
            assertEquals(1, waitingRoom.getPlayers().size());
            assertEquals(player1, waitingRoom.getPlayers().get(0));
        }

        try {
            waitingRoom.addPlayer(player2);
            assertEquals(2, waitingRoom.getPlayers().size());
            assertEquals(player2, waitingRoom.getPlayers().get(1));
            waitingRoom.removePlayer(player2);
            assertEquals(1, waitingRoom.getPlayers().size());
            assertEquals(player1, waitingRoom.getPlayers().get(0));
            waitingRoom.removePlayer(player1);
            assertEquals(0, waitingRoom.getPlayers().size());

        } catch (PlayerAlreadyAddedException e) {
            assertTrue(false);
        }

        try {
            waitingRoom.addPlayer(player1);
            waitingRoom.addPlayer(player2);
            assertEquals(2, waitingRoom.getPlayers().size());
            assertEquals(player1, waitingRoom.getPlayers().get(0));
            assertEquals(player2, waitingRoom.getPlayers().get(1));
            assertTrue(waitingRoom.isTimerRunning());
            waitingRoom.removePlayer(player2);
            assertFalse(waitingRoom.isTimerRunning());
            waitingRoom.addPlayer(player2);
            waitingRoom.addPlayer(player3);
            waitingRoom.removePlayer(player3);
            assertTrue(waitingRoom.isTimerRunning());
            waitingRoom.addPlayer(player3);
            waitingRoom.addPlayer(player4);
            assertFalse(waitingRoom.isTimerRunning()); //TODO control game creation
            assertEquals(0, waitingRoom.getPlayers().size());

        } catch (PlayerAlreadyAddedException e) {
            assertTrue(false);
        }

        try {
            waitingRoom.addPlayer(player1);
            waitingRoom.addPlayer(player2);

            //when timer expires
            if(!waitingRoom.isTimerRunning()) {  //TODO control game creation
                assertEquals(0, waitingRoom.getPlayers().size());
                waitingRoom.addPlayer(player5);
                assertEquals(1, waitingRoom.getPlayers().size());
            }
        } catch (PlayerAlreadyAddedException e) {
            assertTrue(false);
        }
    }
}

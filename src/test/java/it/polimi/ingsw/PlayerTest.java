package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.InvalidDifficultyValueException;
import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;
import it.polimi.ingsw.model.exceptions.UnexpectedMatrixSizeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class PlayerTest {
    private static final String NICKNAME = "Player1";
    private static final String AUTH_TOKEN = "\"c3VwZXJwaXBwbw==\"";
    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private static final int MIN_DIFFICULTY = 3;

    @Test
    void testConstructor() {
        Player player;
        Player player2;
        Player player3;

        player = new Player(NICKNAME, AUTH_TOKEN);
        assertEquals(NICKNAME, player.getNickname());
        assertTrue(player.verifyAuthToken(AUTH_TOKEN));
        assertNull(player.getPrivateGoal());
        assertEquals(0, player.getFavorToken());
        assertFalse(player.isSuspended());

        player2 = new Player(NICKNAME, AUTH_TOKEN);
        assertEquals(player, player2);

        player3 = new Player(NICKNAME + "A", AUTH_TOKEN);
        assertNotEquals(player, player3);
    }

    @Test
    void testGamePlay() {
        Player player;

        Schema schema;
        Constraint[][] constraint;

        try {
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            player = new Player(NICKNAME, AUTH_TOKEN);

            //Set window and favor tokens
            assertDoesNotThrow(() -> player.setWindow(schema));
            assertEquals(schema, player.getWindow().getSchema());
            assertEquals(schema.getDifficulty(), player.getFavorToken());

            //Try negative value for favor token
            try {
                player.useFavorToken(-3);
                fail();
            } catch (InvalidFavorTokenNumberException e) {
                assertTrue(true);
            } catch (NotEnoughFavorTokenException e) {
                fail();
            }

            //Use favor token
            try {
                player.useFavorToken(2);
                assertEquals(schema.getDifficulty() - 2, player.getFavorToken());
            } catch (InvalidFavorTokenNumberException | NotEnoughFavorTokenException e) {
                fail();
            }
            //Use favor token till 0
            try {
                player.useFavorToken(1);
                assertEquals(schema.getDifficulty() - 2 - 1, player.getFavorToken());
            } catch (InvalidFavorTokenNumberException e) {
                assertTrue(true);
            } catch (NotEnoughFavorTokenException e) {
                fail();
            }

            //Use more favor Token than expected
            try {
                player.useFavorToken(5);
                fail();
            } catch (InvalidFavorTokenNumberException e) {
                fail();
            } catch (NotEnoughFavorTokenException e) {
                assertTrue(true);
            }



            //TEST FOR GOALS TO BE DONE

        } catch (InvalidDifficultyValueException | UnexpectedMatrixSizeException e) {
            e.printStackTrace();
        }


    }
}
package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.toolcards.GlazingHammer;
import it.polimi.ingsw.model.toolcards.GrindingStone;
import it.polimi.ingsw.model.toolcards.ToolCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolCardsTest {

    private static final int ROW = 4;
    private static final int COLUMN = 5;

    private Round initRound(){
        List<Player> players = new ArrayList<>();
        List<Dice> draftPool = new ArrayList<>();
        Player player1,player2;
        Schema schema = null;
        String name = "Schema";

        try {
            schema = new Schema(3,new Constraint[ROW][COLUMN], name);
        } catch (InvalidDifficultyValueException e) {
            e.printStackTrace();
        } catch (UnexpectedMatrixSizeException e) {
            e.printStackTrace();
        }
        player1 = new Player("Marco", "jksdkjsd");
        player1.setWindow(schema);
        players.add(player1);
        player2 = new Player("Maria", "9032wsdj");
        player2.setWindow(schema);
        players.add(player2);
        players.add(player2);
        players.add(player1);
        try {
            draftPool.add(new Dice(Color.RED,3));
            draftPool.add(new Dice(Color.BLUE,5));
            draftPool.add(new Dice(Color.GREEN,4));
            draftPool.add(new Dice(Color.GREEN,5));
            draftPool.add(new Dice(Color.PURPLE,1));
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }

        return new Round(draftPool,players);
    }

    @Test
    public void glazingHammerTest(){
        Round round = initRound();
        ToolCard glazingHammer = new GlazingHammer();
        try {
            round.nextPlayer();
        } catch (NoMorePlayersException e) {
            e.printStackTrace();
        }
        assertFalse(glazingHammer.use(round));
        try {
            round.nextPlayer();
            round.nextPlayer();
        } catch (NoMorePlayersException e) {
            e.printStackTrace();
        }
        assertTrue(glazingHammer.use(round));
        assertEquals(2,round.getCurrentPlayer().getFavorToken());
        assertTrue(glazingHammer.use(round));
        assertEquals(0,round.getCurrentPlayer().getFavorToken());
        assertFalse(glazingHammer.use(round));
    }

    @Test
    public void grindingStoneTest(){
        Round round = initRound();
        ToolCard grindingStone = new GrindingStone();
        try {
            round.nextPlayer();
            round.setActiveToolCard(grindingStone);
            round.useDice(0,0, new Dice(Color.RED,3));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(4,round.getCurrentPlayer().getWindow().getCell(0,0).getValue());
        try {
            round.nextPlayer();
            round.setActiveToolCard(grindingStone);
            round.useDice(0,0, new Dice(Color.BLUE,5));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(2,round.getCurrentPlayer().getWindow().getCell(0,0).getValue());
        try {
            round.nextPlayer();
            round.setActiveToolCard(grindingStone);
            round.useDice(0,1, new Dice(Color.GREEN,4));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Can't use toolcard due to not enough favor tokens
        assertEquals(4,round.getCurrentPlayer().getWindow().getCell(0,1).getValue());
        try {
            round.nextPlayer();
            round.setActiveToolCard(grindingStone);
            round.useDice(0,1, new Dice(Color.PURPLE,1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(6,round.getCurrentPlayer().getWindow().getCell(0,1).getValue());
    }

    // TODO: test for FLUX BRUSH require a canBePlaced(Dice d) function into player
}

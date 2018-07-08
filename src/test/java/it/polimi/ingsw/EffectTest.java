package it.polimi.ingsw;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.server.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.toolcards.Effects;
import it.polimi.ingsw.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.utilities.FilesUtil;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionHandlerStub implements ClientInterface {

    private List<Dice> draftPoolDices;
    private List<Coordinate> windowPositions;
    private List<Boolean> booleans;
    private List<Integer> roundTrackIndexes;
    private List<Integer> diceValues;
    private int idxDraftPoolDices, idxWindowPositions, idxBooleans, idxRoundTrackIndexes, idxDiceValues;

    public ConnectionHandlerStub(List<Dice> draftPoolDices,List<Coordinate> windowPositions,List<Boolean> booleans,List<Integer> roundTrackIndexes,List<Integer> diceValues){
        this.draftPoolDices = draftPoolDices;
        this.windowPositions = windowPositions;
        this.booleans = booleans;
        this.roundTrackIndexes = roundTrackIndexes;
        this.diceValues = diceValues;
        idxDraftPoolDices = 0;
        idxWindowPositions = 0;
        idxBooleans = 0;
        idxRoundTrackIndexes = 0;
        idxDiceValues = 0;
    }

    public int getIdxBooleans() {
        return idxBooleans;
    }

    public int getIdxDiceValues() {
        return idxDiceValues;
    }

    public int getIdxWindowPositions() {
        return idxWindowPositions;
    }

    public int getIdxDraftPoolDices() {
        return idxDraftPoolDices;
    }

    public int getIdxRoundTrackIndexes() {
        return idxRoundTrackIndexes;
    }

    public List<Boolean> getBooleans() {
        return booleans;
    }

    public List<Coordinate> getWindowPositions() {
        return windowPositions;
    }

    public List<Dice> getDraftPoolDices() {
        return draftPoolDices;
    }

    public List<Integer> getDiceValues() {
        return diceValues;
    }

    public List<Integer> getRoundTrackIndexes() {
        return roundTrackIndexes;
    }

    @Override
    public void notifyLogin(String nickname) {
    }

    @Override
    public void notifyLogin(List<String> nicknames) {
    }

    @Override
    public void notifyLogout(String nickname) {
    }

    @Override
    public void notifySchemas(List<Schema> schemas) {
    }

    @Override
    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack) {
    }

    @Override
    public void notifyOthersSchemas(Map<String, Schema> playersSchemas) {
    }

    @Override
    public void notifyDicePlaced(String nickname, int row, int column, Dice dice) {
    }

    @Override
    public void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack) {
    }

    @Override
    public void notifyGameInfo(Map<String, Boolean> toolCards, List<String> publicGoals, String privateGoal) {
    }

    @Override
    public void notifyReconInfo(Map<String, Window> windows, Map<String, Integer> favorToken, List<Dice> roundTrack, String cardName) {
    }

    @Override
    public void alertDiceInDraftPool(Dice dice){

    }

    //@Override
    public String getRemoteAddress() {
        return null;
    }

    //@Override
    public void close() {
    }

    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) {
        return windowPositions.get(idxWindowPositions++);
    }

    @Override
    public Dice askDiceDraftPool(String prompt, boolean rollback) {
        return draftPoolDices.get(idxDraftPoolDices++);
    }

    @Override
    public int askDiceRoundTrack(String prompt, boolean rollback) {
        return roundTrackIndexes.get(idxRoundTrackIndexes++);
    }

    @Override
    public boolean askIfPlus(String prompt, boolean rollback) {
        return booleans.get(idxBooleans++);
    }

    @Override
    public int askDiceValue(String prompt, boolean rollback) {
        return diceValues.get(idxDiceValues++);
    }

    @Override
    public int askMoveNumber(String prompt, int n, boolean rollback){
        return 0;
    }

    @Override
    public void notifyEndGame(List<Score> scores){
    }

    @Override
    public void notifySuspension(String nickname){
    }

    @Override
    public void showDice(Dice dice){
    }
}

public class EffectTest {

    private List<Dice> setDraft() {
        List<Dice> fakeDraftPool = new ArrayList<>();
        try {
            fakeDraftPool.add(new Dice(Color.YELLOW, 3));
            fakeDraftPool.add(new Dice(Color.BLUE, 1));
            fakeDraftPool.add(new Dice(Color.RED, 1));
            fakeDraftPool.add(new Dice(Color.GREEN, 2));
            fakeDraftPool.add(new Dice(Color.PURPLE, 6));
            fakeDraftPool.add(new Dice(Color.YELLOW, 5));
        } catch (InvalidDiceValueException e) {

        }
        return fakeDraftPool;
    }

    private Game initGame(int schema) {

        List<Player> players = new ArrayList<>();
        players.add(new Player("lucas", "bWFsYW5kcmlubwo="));
        players.add(new Player("jhonny", "Z3VhbnRvbmUK"));

        JsonParser parser = new JsonParser();
        JsonObject jsonObject;
        Gson gson = new Gson();

        BufferedReader is;
        is = new BufferedReader(new InputStreamReader(FilesUtil.class.getClassLoader().getResourceAsStream("schema"+schema+".json")));

        try {
            jsonObject = parser.parse(is).getAsJsonObject();
            players.get(0).setWindow(gson.fromJson(jsonObject, Schema.class));
            players.get(1).setWindow(gson.fromJson(jsonObject, Schema.class));
        } catch (WindowAlreadySetException e) {
            assertTrue(false);
        }

        try {
            return new Game(players);
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        return null;
    }


    @Test
    public void getDraftedDiceTest() {

        Game game = initGame(1);
        assertNotNull(game);

        Round round = game.getCurrentRound();
        round.fakeDraftPool(setDraft());
        List<Dice> dices = new ArrayList<>();
        try {
            dices.add(new Dice(Color.BLUE, 3));
            dices.add(game.getCurrentRound().getDraftPool().get(0));
            dices.add(new Dice(Color.GREEN, 3));
            dices.add(game.getCurrentRound().getDraftPool().get(1));
            dices.add(game.getCurrentRound().getDraftPool().get(2));
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }

        ConnectionHandlerStub connectionHandlerStub = new ConnectionHandlerStub(dices, null, null, null, null);

        assertFalse(round.isDiceExtracted());
        assertNull(round.getCurrentDiceDrafted());
        assertEquals(0,connectionHandlerStub.getIdxDraftPoolDices());

        assertDoesNotThrow(() -> Effects.getDraftedDice(round, connectionHandlerStub, true));

        assertTrue(round.isDiceExtracted());
        assertEquals(2,connectionHandlerStub.getIdxDraftPoolDices());

        Dice d = null;

        try {
            d = new Dice(Color.YELLOW, 3);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }
        assertEquals(d, round.getCurrentDiceDrafted());
        assertFalse(round.getDraftPool().contains(d));

        try {
            round.nextPlayer();
        } catch (NoMorePlayersException e) {
            e.printStackTrace();
        }

        assertFalse(round.isDiceExtracted());
        assertNull(round.getCurrentDiceDrafted());

        assertDoesNotThrow(() -> Effects.getDraftedDice(round, connectionHandlerStub, true));

        assertTrue(round.isDiceExtracted());
        assertEquals(4,connectionHandlerStub.getIdxDraftPoolDices());


        try {
            d = new Dice(Color.BLUE, 1);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }
        assertEquals(d, round.getCurrentDiceDrafted());
        assertFalse(round.getDraftPool().contains(d));


    }

    @Test
    public void addDiceToWindowTest() {

        Game game = initGame(4);
        assertNotNull(game);

        Round round = game.getCurrentRound();
        List<Coordinate> windowPositions = new ArrayList<>();
        windowPositions.add(new Coordinate(4,1));
        windowPositions.add(new Coordinate(3,1));
        windowPositions.add(new Coordinate(4,2));
        windowPositions.add(new Coordinate(1,1));
        windowPositions.add(new Coordinate(3,2));
        windowPositions.add(new Coordinate(2,1));
        windowPositions.add(new Coordinate(2,3));

        ConnectionHandlerStub connectionHandlerStub = new ConnectionHandlerStub(null, windowPositions, null, null, null);
        Window window = round.getCurrentPlayer().getWindow();

        assertDoesNotThrow(() -> Effects.addDiceToWindow(window,new Dice(Color.BLUE,5), connectionHandlerStub, Window.RuleIgnored.NONE, true));

        assertEquals(1,connectionHandlerStub.getIdxWindowPositions());

        Dice d = null;

        try {
            d = new Dice(Color.BLUE, 5);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }
        assertEquals(d, window.getCell(3,0));

        boolean returned = true;

        try {
            returned = Effects.addDiceToWindow(window,new Dice(Color.BLUE,6), connectionHandlerStub, Window.RuleIgnored.NONE, true);
        } catch (RollbackException | DisconnectionException | InvalidDiceValueException e) {
            assertFalse(true);
        }

        assertFalse(returned);
        assertEquals(1,connectionHandlerStub.getIdxWindowPositions());
        assertNull(window.getCell(2,0));

        assertDoesNotThrow(() -> Effects.addDiceToWindow(window,new Dice(Color.BLUE,3), connectionHandlerStub, Window.RuleIgnored.NONE, true));

        assertEquals(5,connectionHandlerStub.getIdxWindowPositions());

        try {
            d = new Dice(Color.BLUE, 3);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }

        assertEquals(d, window.getCell(2,1));
        assertNull(window.getCell(2,0));
        assertNull(window.getCell(3,1));

    }

    @Test
    public void flipTest(){
        Dice d=null,flipped=null;

        ConnectionHandlerStub stub = new ConnectionHandlerStub(null,null,null,null,null);

        try {
            d = new Dice(Color.BLUE,4);
            flipped = new Dice(Color.BLUE,3);
        } catch (InvalidDiceValueException e) {
            assertFalse(true);
        }

        try {
            Effects.flip(d,stub);
        } catch (DisconnectionException e) {
            assertFalse(true);
        }

        assertEquals(flipped,d);

        try {
            d = new Dice(Color.PURPLE,2);
            flipped = new Dice(Color.PURPLE,5);
        } catch (InvalidDiceValueException e) {
            assertFalse(true);
        }

        try {
            Effects.flip(d,stub);
        } catch (DisconnectionException e) {
            assertFalse(true);
        }

        assertEquals(flipped,d);

        try {
            d = new Dice(Color.YELLOW,5);
            flipped = new Dice(Color.YELLOW,2);
        } catch (InvalidDiceValueException e) {
            assertFalse(true);
        }

        try {
            Effects.flip(d,stub);
        } catch (DisconnectionException e) {
            assertFalse(true);
        }

        assertEquals(flipped,d);

        try {
            d = new Dice(Color.RED,3);
            flipped = new Dice(Color.RED,4);
        } catch (InvalidDiceValueException e) {
            assertFalse(true);
        }

        try {
            Effects.flip(d,stub);
        } catch (DisconnectionException e) {
            assertFalse(true);
        }

        assertEquals(flipped,d);

        try {
            d = new Dice(Color.BLUE,6);
            flipped = new Dice(Color.BLUE,1);
        } catch (InvalidDiceValueException e) {
            assertFalse(true);
        }

        try {
            Effects.flip(d,stub);
        } catch (DisconnectionException e) {
            assertFalse(true);
        }

        assertEquals(flipped,d);

        try {
            d = new Dice(Color.GREEN,1);
            flipped = new Dice(Color.GREEN,6);
        } catch (InvalidDiceValueException e) {
            assertFalse(true);
        }

        try {
            Effects.flip(d,stub);
        } catch (DisconnectionException e) {
            assertFalse(true);
        }

        assertEquals(flipped,d);
    }

    @Test
    public void rerollPoolTest(){
        List<Dice> fakeDraftPool = new ArrayList<>();
        List<Dice> copy = new ArrayList<>();
        try {
            fakeDraftPool.add(new Dice(Color.YELLOW, 3));
            fakeDraftPool.add(new Dice(Color.BLUE, 1));
            fakeDraftPool.add(new Dice(Color.RED, 1));
            fakeDraftPool.add(new Dice(Color.GREEN, 2));
            fakeDraftPool.add(new Dice(Color.PURPLE, 6));
            fakeDraftPool.add(new Dice(Color.YELLOW, 5));
            copy.add(new Dice(Color.YELLOW, 3));
            copy.add(new Dice(Color.BLUE, 1));
            copy.add(new Dice(Color.RED, 1));
            copy.add(new Dice(Color.GREEN, 2));
            copy.add(new Dice(Color.PURPLE, 6));
            copy.add(new Dice(Color.YELLOW, 5));
        } catch (InvalidDiceValueException e) {
            assertFalse(true);
        }

        Effects.rerollPool(fakeDraftPool);

        for(int i=0;i<fakeDraftPool.size();i++){
            assertEquals(copy.get(i).getColor(),fakeDraftPool.get(i).getColor());
            assertTrue(fakeDraftPool.get(i).getValue()>0&&fakeDraftPool.get(i).getValue()<7);
        }
    }

    @Test
    public void swapRoundTrackTest(){
        Game game = initGame(4);
        assertNotNull(game);

        Round round = game.getCurrentRound();
        List<Integer> roundTrack = new ArrayList<>();
        roundTrack.add(-1);
        roundTrack.add(10);
        roundTrack.add(17);
        roundTrack.add(6);
        roundTrack.add(1);
        roundTrack.add(3);
        roundTrack.add(0);

        ConnectionHandlerStub connectionHandlerStub = new ConnectionHandlerStub(null, null, null, roundTrack, null);

        List<Dice> rt = new ArrayList<>();
        Dice d = null;
        Dice d2 = null;

        try {
            d = new Dice(Color.BLUE, 5);
            d2 = new Dice(Color.RED,1);
            rt.add(new Dice(Color.YELLOW,3));
            rt.add(new Dice(Color.BLUE,4));
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }

        round.setCurrentDiceDrafted(d2);
        assertDoesNotThrow(() -> Effects.swapRoundTrack(round,rt,connectionHandlerStub,true));
        assertEquals(5,connectionHandlerStub.getIdxRoundTrackIndexes());

        try {
            d2 = new Dice(Color.BLUE,4);
            assertEquals(d2,round.getCurrentDiceDrafted());
            d2 = new Dice(Color.RED,1);
            assertEquals(d2,rt.get(1));
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }

        round.setCurrentDiceDrafted(d);
        assertDoesNotThrow(() -> Effects.swapRoundTrack(round,rt,connectionHandlerStub,true));
        assertEquals(7,connectionHandlerStub.getIdxRoundTrackIndexes());

        try {
            d = new Dice(Color.YELLOW,3);
            assertEquals(d,round.getCurrentDiceDrafted());
            d = new Dice(Color.BLUE,5);
            assertEquals(d,rt.get(0));
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void setDiceFromBag(){
        Game game = initGame(4);
        assertNotNull(game);

        Round round = game.getCurrentRound();
        List<Integer> values = new ArrayList<>();
        values.add(-1);
        values.add(0);
        values.add(17);
        values.add(6);
        values.add(1);
        values.add(3);
        values.add(0);

        ConnectionHandlerStub connectionHandlerStub = new ConnectionHandlerStub(null, null, null, null, values);

        Dice d = null;
        Dice d2 = null;

        try {
            d2 = new Dice(Color.BLUE,6);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }


        assertNull(round.getCurrentDiceDrafted());

        assertDoesNotThrow(() -> Effects.setDiceFromBag(round,new Dice(Color.BLUE, 5),connectionHandlerStub,true));
        assertEquals(4,connectionHandlerStub.getIdxDiceValues());
        assertEquals(d2,round.getCurrentDiceDrafted());

        try {
            d2 = new Dice(Color.RED,1);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }

        assertDoesNotThrow(() -> Effects.setDiceFromBag(round,new Dice(Color.RED,4),connectionHandlerStub,true));
        assertEquals(5,connectionHandlerStub.getIdxDiceValues());
        assertEquals(d2,round.getCurrentDiceDrafted());

    }
}
package it.polimi.ingsw;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PrivateGoal;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;
import it.polimi.ingsw.network.server.ConnectionHandler;
import it.polimi.ingsw.utilities.FilesUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionHandlerStub implements ConnectionHandler {

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
    public void notifyGameInfo(List<ToolCard> toolCards, List<PublicGoal> publicGoals, PrivateGoal privateGoal) {
    }

    @Override
    public void notifyReconInfo(HashMap<String, Window> windows, HashMap<String, Integer> favorToken, List<Dice> roundTrack) {
    }

    @Override
    public String getRemoteAddress() {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public Coordinate askDiceWindow(String prompt) {
        return windowPositions.get(idxWindowPositions++);
    }

    @Override
    public Dice askDiceDraftPool(String prompt) {
        return draftPoolDices.get(idxDraftPoolDices++);
    }

    @Override
    public int askDiceRoundTrack(String prompt) {
        return roundTrackIndexes.get(idxRoundTrackIndexes++);
    }

    @Override
    public boolean askIfPlus(String prompt) {
        return booleans.get(idxBooleans++);
    }

    @Override
    public int askDiceValue(String prompt) {
        return diceValues.get(idxDiceValues++);
    }
}

public class ToolcardTest {

    private List<Dice> setDraft(){
        List<Dice> fakeDraftPool = new ArrayList<>();
        try {
            fakeDraftPool.add(new Dice(Color.YELLOW, 3));
            fakeDraftPool.add(new Dice(Color.BLUE, 1));
            fakeDraftPool.add(new Dice(Color.RED, 1));
            fakeDraftPool.add(new Dice(Color.GREEN, 2));
            fakeDraftPool.add(new Dice(Color.PURPLE, 6));
            fakeDraftPool.add(new Dice(Color.YELLOW,5));
        } catch (InvalidDiceValueException e) {

        }
        return fakeDraftPool;
    }

    private Game initGame(int schema){

        List<Player> players = new ArrayList<>();
        players.add(new Player("lucas", "bWFsYW5kcmlubwo="));
        players.add(new Player("jhonny", "Z3VhbnRvbmUK" ));

        List<File> files = FilesUtil.listFiles(FilesUtil.SCHEMA_FOLDER);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject;
        Gson gson = new Gson();

        File file = null;
        for(File f : files){
            if(f.getName().equals(schema+".json")){
                file = f; break;
            }
        }

        try {
            jsonObject = parser.parse(new FileReader(file)).getAsJsonObject();
            players.get(0).setWindow(gson.fromJson(jsonObject, Schema.class));
            players.get(1).setWindow(gson.fromJson(jsonObject, Schema.class));
        } catch (FileNotFoundException e) {
            assertTrue(false);
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

    public ToolCard getToolCard(String name){
        List<File> files = FilesUtil.listFiles(FilesUtil.TOOLCARD_FOLDER);
        File file = null;

        for(File f : files){
            if(f.getName().equals(name)){
                file = f; break;
            }
        }

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = null;

        try {
            jsonObject = parser.parse(new FileReader(file)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            assertTrue(false);
        }
        return new ToolCard(jsonObject);
    }


    /* Test description :
     *
     * Used, 1 invalid dice from draft pool, try to place a dice afterward and got fail
     * Not used, already placed a Dice
     * Used, 1 invalid dice from draft pool, 1 invalid action (use - on value 1), 1 invalid window position
     * Used
     * Used, 2 invalid action (use + on value 6)
     * Not used, not enough favor token
     */
    @Test
    public void tc1Test(){
        ToolCard tool = getToolCard("pinza-sgrossatrice.json");
        Game game = initGame(1);

        assertNotNull(tool);
        assertNotNull(game);

        game.getCurrentRound().fakeDraftPool(setDraft());
        List<Dice> dices = new ArrayList<>();
        try {
            dices.add(new Dice(Color.BLUE,3));
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }
        dices.add(game.getCurrentRound().getDraftPool().get(0));
        dices.add(game.getCurrentRound().getDraftPool().get(1));
        dices.add(game.getCurrentRound().getDraftPool().get(2));
        dices.add(game.getCurrentRound().getDraftPool().get(3));
        dices.add(game.getCurrentRound().getDraftPool().get(4));

        List<Boolean> booleans = new ArrayList<>();
        booleans.add(true);
        booleans.add(false);
        booleans.add(true);
        booleans.add(true);
        booleans.add(true);
        booleans.add(true);
        booleans.add(false);

        List<Coordinate> windowPositions = new ArrayList<>();
        windowPositions.add(new Coordinate(0,0));
        windowPositions.add(new Coordinate(0,1));
        windowPositions.add(new Coordinate(0,2));
        windowPositions.add(new Coordinate(1,0));
        windowPositions.add(new Coordinate(1,2));

        ConnectionHandlerStub connectionHandlerStub = new ConnectionHandlerStub(dices,windowPositions,booleans,null,null);

        assertDoesNotThrow(()->tool.use(game,connectionHandlerStub));
        assertEquals(3,game.getCurrentRound().getCurrentPlayer().getFavorToken());
        assertEquals(2,connectionHandlerStub.getIdxDraftPoolDices());
        assertEquals(1,connectionHandlerStub.getIdxBooleans());
        assertEquals(1,connectionHandlerStub.getIdxWindowPositions());
        final Dice d = new Dice(Color.RED);
        Dice expected = null;
        try {
            expected = new Dice(Color.YELLOW,4);
        } catch (InvalidDiceValueException e) {

        }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(0,0));
        assertThrows(DiceAlreadyExtractedException.class,()->game.placeDice(0,1,d));

        try {
            game.getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        assertDoesNotThrow(()->game.placeDice(0,3,new Dice(Color.BLUE,1)));
        assertThrows(AlreadyDraftedException.class,()->tool.use(game,connectionHandlerStub));

        try {
            game.getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        assertDoesNotThrow(()->tool.use(game,connectionHandlerStub));
        assertEquals(2,game.getCurrentRound().getCurrentPlayer().getFavorToken());
        assertEquals(4,connectionHandlerStub.getIdxDraftPoolDices());
        assertEquals(3,connectionHandlerStub.getIdxBooleans());
        assertEquals(3,connectionHandlerStub.getIdxWindowPositions());
        try {
            expected = new Dice(Color.RED,2);
        } catch (InvalidDiceValueException e) {

        }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(0,2));


        try {
            game.getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        assertDoesNotThrow(()->tool.use(game,connectionHandlerStub));
        assertEquals(1,game.getCurrentRound().getCurrentPlayer().getFavorToken());
        assertEquals(5,connectionHandlerStub.getIdxDraftPoolDices());
        assertEquals(4,connectionHandlerStub.getIdxBooleans());
        assertEquals(4,connectionHandlerStub.getIdxWindowPositions());
        try {
            expected = new Dice(Color.GREEN,3);
        } catch (InvalidDiceValueException e) {

        }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(1,0));

        game.nextRound();
        game.getCurrentRound().fakeDraftPool(setDraft());

        assertDoesNotThrow(()->tool.use(game,connectionHandlerStub));
        assertEquals(0,game.getCurrentRound().getCurrentPlayer().getFavorToken());
        assertEquals(6,connectionHandlerStub.getIdxDraftPoolDices());
        assertEquals(7,connectionHandlerStub.getIdxBooleans());
        assertEquals(5,connectionHandlerStub.getIdxWindowPositions());
        try {
            expected = new Dice(Color.PURPLE,5);
        } catch (InvalidDiceValueException e) {

        }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(1,2));

        try {
            game.getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        assertThrows(NotEnoughFavorTokenException.class,()->tool.use(game,connectionHandlerStub));
    }

    @Test
    public void tc2Test(){
        ToolCard tool = getToolCard("pennello-per-eglomise.json");
        Game game = initGame(1);

        assertNotNull(tool);
        assertNotNull(game);

        game.getCurrentRound().fakeDraftPool(setDraft());
        List<Coordinate> windowPositions = new ArrayList<>();
        windowPositions.add(new Coordinate(0,0));
        windowPositions.add(new Coordinate(0,2));
        windowPositions.add(new Coordinate(0,2));
        windowPositions.add(new Coordinate(3,0));
        windowPositions.add(new Coordinate(0,0));
        windowPositions.add(new Coordinate(1,1));
        windowPositions.add(new Coordinate(0,0));
        windowPositions.add(new Coordinate(2,2));

        ConnectionHandlerStub connectionHandlerStub = new ConnectionHandlerStub(null,windowPositions,null,null,null);

        assertThrows(NoDiceInWindowException.class,()->tool.use(game,connectionHandlerStub));

        assertDoesNotThrow(()->game.placeDice(0,2, new Dice(Color.BLUE,1)));
        assertDoesNotThrow(()->tool.use(game,connectionHandlerStub));
        assertEquals(3,game.getCurrentRound().getCurrentPlayer().getFavorToken());
        assertEquals(5,connectionHandlerStub.getIdxWindowPositions());
        Dice expected = null;
        try {
            expected = new Dice(Color.BLUE,1);
        } catch (InvalidDiceValueException e) { }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(0,0));
        for(int r = 0;r<Window.ROW;r++)
            for(int c = 0;c<Window.COLUMN;c++)
                if(c!=0||r!=0) assertEquals(null,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(r,c));

        try {
            game.getCurrentRound().nextPlayer();
            game.getCurrentRound().nextPlayer();
            game.getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        assertDoesNotThrow(()->game.placeDice(1,1,new Dice(Color.RED,1)));
        assertDoesNotThrow(()->tool.use(game,connectionHandlerStub));
        assertEquals(1,game.getCurrentRound().getCurrentPlayer().getFavorToken());
        assertEquals(8,connectionHandlerStub.getIdxWindowPositions());
        try {
            expected = new Dice(Color.RED,1);
        } catch (InvalidDiceValueException e) { }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(1,1));
        try {
            expected = new Dice(Color.BLUE,1);
        } catch (InvalidDiceValueException e) { }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(2,2));
        assertEquals(null,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(0,0));

        assertThrows(NotEnoughFavorTokenException.class,()->tool.use(game,connectionHandlerStub));
    }

    @Test
    public void tc11Test(){
        ToolCard tool = getToolCard("diluente-per-pasta-salda.json");
        Game game = initGame(4);

        assertNotNull(tool);
        assertNotNull(game);

        game.getCurrentRound().fakeDraftPool(setDraft());

        List<Dice> dices = new ArrayList<>();
        dices.add(game.getCurrentRound().getDraftPool().get(1));
        dices.add(game.getCurrentRound().getDraftPool().get(1));
        dices.add(game.getCurrentRound().getDraftPool().get(1));
        dices.add(game.getCurrentRound().getDraftPool().get(2));
        dices.add(game.getCurrentRound().getDraftPool().get(3));
        dices.add(game.getCurrentRound().getDraftPool().get(4));

        List<Coordinate> windowPositions = new ArrayList<>();
        windowPositions.add(new Coordinate(0,0));
        windowPositions.add(new Coordinate(1,1));
        windowPositions.add(new Coordinate(0,2));
        windowPositions.add(new Coordinate(2,2));

        List<Integer> diceValues = new ArrayList<>();
        diceValues.add(-4);
        diceValues.add(8);
        diceValues.add(5);
        diceValues.add(3);
        diceValues.add(5);

        ConnectionHandlerStub connectionHandlerStub = new ConnectionHandlerStub(dices,windowPositions,null,null,diceValues);

        assertDoesNotThrow(()->game.placeDice(3,0, new Dice(Color.YELLOW,5)));
        assertThrows(AlreadyDraftedException.class,()->tool.use(game,connectionHandlerStub));

        try {
            game.getCurrentRound().nextPlayer();
            game.getCurrentRound().nextPlayer();
            game.getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            assertTrue(false);
        }

        Dice expected = null;
        try {
            expected = new Dice(Color.YELLOW,5);
        } catch (InvalidDiceValueException e) { }
        assertEquals(expected,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(3,0));
        for(int r = 0;r<Window.ROW;r++)
            for(int c = 0;c<Window.COLUMN;c++)
                if(c!=0||r!=3) assertEquals(null,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(r,c));


        assertDoesNotThrow(()->tool.use(game,connectionHandlerStub));
        assertEquals(4,game.getCurrentRound().getCurrentPlayer().getFavorToken());
        assertEquals(1,connectionHandlerStub.getIdxDraftPoolDices());
        assertEquals(3,connectionHandlerStub.getIdxDiceValues());
        assertEquals(0,connectionHandlerStub.getIdxWindowPositions());
        assertEquals(null,game.getCurrentRound().getCurrentPlayer().getWindow().getCell(2,1));
        assertEquals(null,game.getCurrentRound().getCurrentDiceDrafted());
        assertEquals(5,game.getCurrentRound().getDraftPool().get(game.getCurrentRound().getDraftPool().size()-1).getValue());

    }
}

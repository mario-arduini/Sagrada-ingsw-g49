package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.toolcards.ToolCard;
import it.polimi.ingsw.network.server.ConnectionHandler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameFlowHandler {
    private User player;
    private Game game;
    private GamesHandler gamesHandler;
    private List<Schema> initialSchemas;
    private ToolCard activeToolCard;

    public GameFlowHandler(GamesHandler gamesHandler){
        this.player = null;
        this.game = null;
        this.gamesHandler = gamesHandler;
        this.activeToolCard = null;
    }

    public void setGame(Game game) {
        this.game = game;
        initialSchemas = game.extractSchemas();
        player.notifySchemas(initialSchemas);
    }

    //TODO: Shouldn't be able to choose a schema more than once
    public void chooseSchema(Integer schemaNumber){
        player.setWindow(initialSchemas.get(schemaNumber));
    }

    public void placeDice(int row, int column, Dice dice) throws NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        if (!game.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        game.placeDice(row, column, dice);
    }

    public void notifyDicePlaced(int row, int column, Dice dice){
        gamesHandler.notifyAllDicePlaced(game, player.getNickname(), row, column, dice);
    }

    public void pass() throws NotYourTurnException{
        if (!game.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        this.activeToolCard = null;
        gamesHandler.goOn(game);
    }

    public void checkGameReady(){
        List<Player> inGamePlayers = game.getPlayers();
        for (Player p: inGamePlayers)
            if (p.getWindow()==null)
                return;
        gamesHandler.gameReady(game);
    }

    public void disconnected(){
        if (game == null){
            gamesHandler.waitingRoomDisconnection(player);
        }
    }

    public List<String> getPlayers(){
        return game == null ? gamesHandler.getWaitingPlayers() : game.getPlayers().stream().map(Player::getNickname).collect(Collectors.toList());
    }

    public boolean login(String nickname, String password, ConnectionHandler connection) {
        this.player = gamesHandler.login(nickname, password, connection);
        return this.player != null;
    }

    public void logout() {
        gamesHandler.logout(this.player.getNickname());
    }

    public void useToolCard(String cardName) throws NoSuchToolCardException, InvalidDiceValueException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException {
        if (!game.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        Optional<ToolCard> fetch = game.getToolCards().stream().filter(card -> card.getName().equalsIgnoreCase(cardName)).findFirst();
        if (!fetch.isPresent()){
            throw new NoSuchToolCardException();
        }
        this.activeToolCard = fetch.get();
        this.activeToolCard.use(game);
        gamesHandler.notifyAllToolCardUsed(game, player.getNickname(), activeToolCard.getName());
    }
}

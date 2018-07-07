package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;
import it.polimi.ingsw.network.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.network.server.Logger;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A GamesHandler is a sort of a database of every game that is being played.
 * It stores the status of the waiting room and keeps track of every user that is playing.
 * Every login or reconnection is achieved via an object of this class.
 */
public class GamesHandler {
    private List<GameFlowHandler> waitingRoom;
    private List<GameFlowHandler> playingUsers;
    private static final String TIMEOUT_FILE_NAME = "timeout.txt";
    private int secondsTimer;
    private Timer timer;

    //TODO: DONE, check for file exception
    public GamesHandler(){
        this.waitingRoom = new ArrayList<>();
        this.playingUsers = new ArrayList<>();
        this.secondsTimer = readIntFromFile(TIMEOUT_FILE_NAME);
        //TODO: Throw exception if file does not exist
    }

    /**
     * Logs a player in a waiting room or in an existing game in case he was playing.
     * @param nickname name of the player that wants to login.
     * @param password token session of the player that wants to login.
     * @param connection connection of the player that wants to login.
     * @return the new/old GameFlowHandler of the player that wanted to login.
     * @throws LoginFailedException if player already exists with a different token.
     * @throws RemoteException on RMI problems.
     */
    public synchronized GameFlowHandler login(String nickname, String password, ClientInterface connection) throws LoginFailedException, RemoteException {
        Player user;
        GameFlowHandler newGameFlow;
        if (this.findGameFlow(nickname).isPresent())
            return reconnection(nickname, password, connection);

        waitingRoom.forEach(gameFlow -> {
            try {
                gameFlow.getConnection().notifyLogin(nickname);
            } catch (RemoteException e) {
                Logger.print(e.toString());
            }
        });
        user = new Player(nickname, password);
        connection.notifyLogin(getWaitingPlayers());
        Logger.print("Logged in: " + nickname);
        newGameFlow = new GameFlowHandler(this, connection, user);
        waitingRoom.add(newGameFlow);
        waitingRoomNewPlayer();
        return newGameFlow;
    }

    /**
     * Given the name of a player returns its GameFlowHandler.
     * @param nickname of the desired player.
     * @return Optional GameFlowHandler.
     */
    private synchronized Optional<GameFlowHandler> findGameFlow(String nickname){
        List <GameFlowHandler> allGameFlows = new ArrayList<>();
        allGameFlows.addAll(this.waitingRoom);
        allGameFlows.addAll(this.playingUsers);
        return allGameFlows.stream().filter(gameFlow -> gameFlow.getPlayer().getNickname().equalsIgnoreCase(nickname)).findFirst();
    }

    /**
     * Reconnect a player to its GameFlowHandler if the player already exists.
     * Updates the connection of the GameFlowHandler.
     * Checks the token session.
     * @param nickname name of the player that wants to reconnect.
     * @param password session token of the player that wants to reconnect
     * @param connection new connection of the player that wants to reconnect
     * @return the GameFlowHandler of the player that was reconnected to the game.
     * @throws LoginFailedException if name-token does not match saved name-token.
     */
    private synchronized GameFlowHandler reconnection(String nickname, String password, ClientInterface connection) throws LoginFailedException {
        Optional<GameFlowHandler> gameFlowFetched = findGameFlow(nickname);
        GameFlowHandler gameFlow;

        if (gameFlowFetched.isPresent()){
            gameFlow = gameFlowFetched.get();
            if (playingUsers.contains(gameFlow) && gameFlow.getPlayer().verifyAuthToken(password)){
                gameFlow.reconnection(connection);

                return gameFlow;
            }
        }
        throw new LoginFailedException();
    }

    /**
     * Log out a player from a waiting room or from a game.
     * Makes its nick available again.
     * @param nickname name of the player that logged out.
     */
    public synchronized void logout(String nickname){
        Optional<GameFlowHandler> gameFlowFetched = findGameFlow(nickname);
        GameFlowHandler gameFlow;
        if (gameFlowFetched.isPresent()) {
            gameFlow = gameFlowFetched.get();
            if (waitingRoom.contains(gameFlow)) {
                waitingRoom.remove(gameFlow);
                waitingRoom.forEach(user -> {
                    try {
                        user.getConnection().notifyLogout(nickname);
                    } catch (RemoteException e) {
                        Logger.print(e.toString());
                    }
                });
                waitingRoomDisconnection(gameFlow);
            } else {
                playingUsers.remove(gameFlow);
            }
            Logger.print("Logged out: " + gameFlow.getPlayer().getNickname());
        }else {
            Logger.print("Logout failed: " + nickname);
        }
    }

    /**
     * Check if the game is ready to start.
     * If there are >2 players a timer is set.
     * If there are 4 player a game is started.
     */
    private synchronized void waitingRoomNewPlayer(){
        if(waitingRoom.size() == 2) {
            timer = new Timer();
            timer.schedule(new GamesHandler.TimerExpired(), (long) secondsTimer * 1000);
        }
        else if(waitingRoom.size() >= 4)
            startGame();
    }

    /**
     * Remove a player from the waiting room.
     * Nickname is available again.
     * @param gameFlow the GameFlowHandler of the player that is leaving the waiting room.
     */
    public synchronized void waitingRoomDisconnection(GameFlowHandler gameFlow){
        waitingRoom.remove(gameFlow);
        waitingRoom.forEach(player -> {
            try {
                player.getConnection().notifyLogout(gameFlow.getPlayer().getNickname());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        if(waitingRoom.size() < 2 && timer != null) {
            timer.cancel();
        }
    }

    /**
     * Spawns a GameRoom and cleans the waiting room.
     * Every GameFlowHandler gets a game set.
     */
    private synchronized void startGame() {
        List<Player> playerList;
        List<ClientInterface> connections;
        GameRoom game;
        try {
            playerList = waitingRoom.stream().map(GameFlowHandler::getPlayer).collect(Collectors.toList());
            connections = waitingRoom.stream().map(GameFlowHandler::getConnection).collect(Collectors.toList());
            game = new GameRoom(playerList, connections);
            waitingRoom.forEach(user -> user.setGame(game));
            playingUsers.addAll(waitingRoom);
            Logger.print(String.format("Game Started: %s", getWaitingPlayers().toString()));

        } catch (NoMorePlayersException e) {
            Logger.print(String.format("Game couldn't start because of Round, kicking out %s", getWaitingPlayers().toString()));
        }finally {
            waitingRoom.clear();
            if (timer != null)
                timer.cancel();
        }
    }

    /**
     * Returns a list of all the players in the waiting room.
     * @return List of players nicknames.
     */
    public synchronized List<String> getWaitingPlayers() {
        return waitingRoom.stream().map(GameFlowHandler::getPlayer).map(Player::getNickname).collect(Collectors.toList());
    }

    /**
     * Timer Class to start games with n. of players >2 && <4.
     */
    class TimerExpired extends TimerTask {
        public void run() {
            startGame();
        }
    }

    /**
     * Puts a GameFlowHandler from a Game back to a Waiting Room.
     * @param gameFlow the gameFlow that is leaving a game.
     */
    public synchronized void goToWaitingRoom(GameFlowHandler gameFlow){
        if (waitingRoom.contains(gameFlow)) return;
        playingUsers.remove(gameFlow);
        try {
            gameFlow.getConnection().notifyLogin(getWaitingPlayers());
        } catch (Exception e) {
            return;
        }

        waitingRoom.forEach(gameF -> {
            try {
                gameF.getConnection().notifyLogin(gameFlow.getPlayer().getNickname());
            } catch (RemoteException e) {
                Logger.print(e.toString());
            }
        });        waitingRoom.add(gameFlow);

        waitingRoomNewPlayer();
    }


    //TODO: move to utils (?)
    private int readIntFromFile(String filename){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(GamesHandler.class.getClassLoader().getResourceAsStream(filename)));
            String text;

            if ((text = reader.readLine()) != null)
                return Integer.parseInt(text);

        } catch (IOException e) {
            Logger.print("Read Int From File: parsing " + e.getMessage());
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Logger.print("Read Int From File: closing " + e.getMessage());
            }
        }
        return -1;
    }
}

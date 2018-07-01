package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.server.Logger;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

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
        String filename = getClass().getClassLoader().getResource(TIMEOUT_FILE_NAME).getFile();
        this.secondsTimer = readIntFromFile(filename);
        //TODO: Throw exception if file does not exist
    }

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
        //TODO: Have a look at this notify, socket vs RMI
        connection.notifyLogin(getWaitingPlayers());
        Logger.print("Logged in: " + nickname); //+ " " + connection.getRemoteAddress());
        newGameFlow = new GameFlowHandler(this, connection, user);
        waitingRoom.add(newGameFlow);
        waitingRoomNewPlayer();
        return newGameFlow;
    }

    private synchronized Optional<GameFlowHandler> findGameFlow(String nickname){
        List <GameFlowHandler> allGameFlows = new ArrayList<>();
        allGameFlows.addAll(this.waitingRoom);
        allGameFlows.addAll(this.playingUsers);
        return allGameFlows.stream().filter(gameFlow -> gameFlow.getPlayer().getNickname().equalsIgnoreCase(nickname)).findFirst();
    }

    private synchronized GameFlowHandler reconnection(String nickname, String password, ClientInterface connection) throws LoginFailedException {
        Optional<GameFlowHandler> gameFlowFetched = findGameFlow(nickname);
        GameFlowHandler gameFlow;

        if (gameFlowFetched.isPresent()){
            gameFlow = gameFlowFetched.get();
            if (playingUsers.contains(gameFlow) && gameFlow.getPlayer().verifyAuthToken(password)){
                gameFlow.reconnection(connection);
                Logger.print("Reconnected: " + nickname); //+ " " + connection.getRemoteAddress());
                List<String> users = gameFlow.getPlayers();
                users = users.stream().filter(nick -> !nick.equalsIgnoreCase(nickname)).collect(Collectors.toList());
                try {
                    connection.notifyLogin(users);
                } catch (RemoteException e) {
                    Logger.print(e.toString());
                }
                return gameFlow;
            }
        }
        throw new LoginFailedException();
    }

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
            Logger.print("Logged out: " + gameFlow.getPlayer().getNickname()); //+ " " + gameFlow.getConnection().getRemoteAddress());
        }else {
            Logger.print("Logout failed: " + nickname);
        }
    }

    //TODO: move to utils (?)
    private int readIntFromFile(String filename){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(filename)));
            String text;

            if ((text = reader.readLine()) != null)
                return Integer.parseInt(text);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private synchronized void waitingRoomNewPlayer(){
        if(waitingRoom.size() == 2) {
            timer = new Timer();
            timer.schedule(new GamesHandler.TimerExpired(), (long) secondsTimer * 1000);
        }
        else if(waitingRoom.size() >= 4)
            startGame();
    }

    public synchronized void waitingRoomDisconnection(GameFlowHandler gameFlow){
        waitingRoom.remove(gameFlow);
        if(waitingRoom.size() < 2 && timer != null) {
            timer.cancel();
        }
    }

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

    public synchronized List<String> getWaitingPlayers() {
        return waitingRoom.stream().map(GameFlowHandler::getPlayer).map(Player::getNickname).collect(Collectors.toList());
    }

    class TimerExpired extends TimerTask {
        public void run() {
            startGame();
        }
    }

    public synchronized void goToWaitingRoom(GameFlowHandler gameFlow){
        if (waitingRoom.contains(gameFlow)) return;
        playingUsers.remove(gameFlow);
        waitingRoom.add(gameFlow);
        waitingRoomNewPlayer();
    }

}

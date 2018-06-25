package it.polimi.ingsw.network.client.model;

public class Score {
    private String player;
    private int totalScore;
    private int privateScore;
    private int publicScore;
    private int favorToken;
    private int emptyCells;
    private int roundPosition;

    Score(String player, int publicScore, int privateScore, int favorToken, int emptyCells, int roundPosition){
        this.player = player;
        this.privateScore = privateScore;
        this.publicScore = publicScore;
        this.favorToken = favorToken;
        this.emptyCells = emptyCells;
        this.totalScore = privateScore + publicScore + favorToken - emptyCells;
        this.roundPosition = roundPosition;
    }

    public String getPlayer(){
        return this.player;
    }

    public int getTotalScore(){
        return this.totalScore;
    }

    public int getPublicScore(){
        return this.publicScore;
    }

    public int getPrivateScore(){
        return this.privateScore;
    }

    public int getFavorToken(){
        return this.favorToken;
    }

    public int getEmptyCells(){
        return this.emptyCells;
    }

    public int getRoundPosition(){
        return this.roundPosition;
    }
}

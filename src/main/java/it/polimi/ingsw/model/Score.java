package it.polimi.ingsw.model;

import it.polimi.ingsw.network.server.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        this.totalScore = computeScore();
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

    private int computeScore(){
        return privateScore + publicScore + favorToken - emptyCells;
    }

    public static List<Score> sort(List<Score> scores){
        List<Score> sortedScores = new ArrayList<>(scores);

        //Sort by Total Score
        sortedScores.sort(Comparator.comparingInt(Score::getTotalScore).reversed());
        Logger.print("Total Score: " + sortedScores.stream().map(Score::getPlayer).collect(Collectors.toList()));

        //Sort by Private Goal
        sortedScores = Score.partialSort(sortedScores, Comparator.comparing(Score::getPrivateScore).reversed());
        Logger.print("Private Score: " + sortedScores.stream().map(Score::getPlayer).collect(Collectors.toList()));
        //Sort by Favor Token
        sortedScores = Score.partialSort(sortedScores, Comparator.comparing(Score::getFavorToken).reversed());
        Logger.print("Favor Score: " + sortedScores.stream().map(Score::getPlayer).collect(Collectors.toList()));
        //Sort by Round position
        sortedScores = Score.partialSort(sortedScores, Comparator.comparing(Score::getRoundPosition).reversed());
        Logger.print("Round Score" + sortedScores.stream().map(Score::getPlayer).collect(Collectors.toList()));
        return sortedScores;
    }

    private static List<Score> partialSort(List<Score> scores, Comparator<Score> comparator){
        List<Score> tmpScores = new ArrayList<>(scores.stream().filter(score -> score.getTotalScore() == scores.get(0).getTotalScore()).sorted(comparator).collect(Collectors.toList()));
        if (tmpScores.size()!=scores.size())
            tmpScores.addAll(scores.subList(tmpScores.size(), scores.size()));
        return tmpScores;
    }

}

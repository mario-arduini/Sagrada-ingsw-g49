package it.polimi.ingsw.model;

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
        //Sort by Private Goal
        sortedScores = Score.partialSort(sortedScores, Comparator.comparing(Score::getPrivateScore).reversed());
        //Sort by Favor Token
        sortedScores = Score.partialSort(sortedScores, Comparator.comparing(Score::getFavorToken).reversed());
        //Sort by Round position
        sortedScores = Score.partialSort(sortedScores, Comparator.comparing(Score::getRoundPosition).reversed());

        return sortedScores;
    }

    private static List<Score> partialSort(List<Score> scores, Comparator<Score> comparator){
        List<Score> tmpScores = new ArrayList<>();
        tmpScores.addAll(scores);
        tmpScores.addAll(scores.stream().filter(score -> score.getTotalScore()==scores.get(0).getTotalScore()).sorted(comparator).collect(Collectors.toList()));
        tmpScores.addAll(tmpScores.subList(tmpScores.size(), scores.size()));
        return tmpScores;
    }

}

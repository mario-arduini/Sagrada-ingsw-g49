package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class representing the score of a player in the game
 */
public class Score implements Serializable {
    private String player;
    private int totalScore;
    private int privateScore;
    private int publicScore;
    private int favorToken;
    private int emptyCells;
    private int roundPosition;

    /**
     * Construct the Score object
     * @param player Nickname of the player
     * @param publicScore score from public goals
     * @param privateScore score from private goal
     * @param favorToken number of favor token
     * @param emptyCells number of empty cells
     * @param roundPosition final round position
     */
    Score(String player, int publicScore, int privateScore, int favorToken, int emptyCells, int roundPosition){
        this.player = player;
        this.privateScore = privateScore;
        this.publicScore = publicScore;
        this.favorToken = favorToken;
        this.emptyCells = emptyCells;
        this.totalScore = computeScore();
        this.roundPosition = roundPosition;
    }

    /**
     * Get player nickname
     * @return Nickname of the player
     */
    public String getPlayer(){
        return this.player;
    }

    /**
     * Get total score of the player
     * @return total score
     */
    public int getTotalScore(){
        return this.totalScore;
    }

    /**
     * Get public goals' score of the player
     * @return public goals' score
     */
    public int getPublicScore(){
        return this.publicScore;
    }

    /**
     * Get private goal's score of the player
     * @return private goal's score
     */
    public int getPrivateScore(){
        return this.privateScore;
    }

    /**
     * Get favor token
     * @return number of favor token
     */
    public int getFavorToken(){
        return this.favorToken;
    }

    /**
     * Get number of empty cells
     * @return number of empty cells
     */
    public int getEmptyCells(){
        return this.emptyCells;
    }

    /**
     * Get round position
     * @return round position
     */
    public int getRoundPosition(){
        return this.roundPosition;
    }

    /**
     * Compute Total Score
     * @return Total score
     */
    private int computeScore(){
        return privateScore + publicScore + favorToken - emptyCells;
    }

    /**
     * Sort the scores starting from the winner
     * @param scores Lis to Score to sort
     * @return Sorted List
     */
    static List<Score> sort(List<Score> scores){
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

    /**
     * In case of tie sort by another parameter
     * @param scores List of Scores
     * @param comparator Comparator to use to order
     * @return Sorted List of Scores
     */
    private static List<Score> partialSort(List<Score> scores, Comparator<Score> comparator){
        List<Score> tmpScores = new ArrayList<>(scores.stream().filter(score -> score.getTotalScore() == scores.get(0).getTotalScore()).sorted(comparator).collect(Collectors.toList()));
        if (tmpScores.size()!=scores.size())
            tmpScores.addAll(scores.subList(tmpScores.size(), scores.size()));
        return tmpScores;
    }

}

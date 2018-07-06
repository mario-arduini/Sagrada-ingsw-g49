package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;

import java.io.Serializable;
import java.util.Random;

/***
 * Contains information about a single dice, with its color and current value
 * Provides methods to get information about the dice and to change its value
 */
public class Dice implements Serializable {
    private final Color color;
    private int value;

    /***
     * This constructor create a dice with a given color
     * @param color the color of the dice
     */
    public Dice(Color color){
        this.color = color;
    }

    /***
     * This constructor create a dice with a given color and number
     * @param color the color of the dice
     * @param value the number of the dice
     * @throws InvalidDiceValueException if the value is less than 1 or greater than 6
     */
    public Dice(Color color, int value) throws InvalidDiceValueException {
        if(value < 1 || value > 6)
            throw  new InvalidDiceValueException();
        this.color = color;
        this.value = value;
    }

    /***
     * This constructor create a dice equals to a given dice
     * @param dice the dice to clone
     */
    public Dice(Dice dice){
        this.value = dice.value;
        this.color = dice.color;
    }

    /***
     * This method is used to the get the color of the dice
     * @return the color of the dice
     */
    public Color getColor(){
        return color;
    }

    /***
     * This method is used to the get the value of the dice
     * @return the value of the dice
     */
    public Integer getValue(){
        return value;
    }

    /***
     * Sets a value to the dice
     * @param value the new value of the dice
     * @throws InvalidDiceValueException if the value is less than 1 or greater than 6
     */
    public void setValue(int value) throws InvalidDiceValueException {
        if(value < 1 || value > 6)
            throw  new InvalidDiceValueException();
        this.value = value;
    }

    /***
     * Sets a new value to the dice randomly
     */
    public void roll(){
        Random diceRoller = new Random();
        this.value = diceRoller.nextInt(6) + 1;
    }

    /***
     * Confronts the dice with a given one
     * @param dice the dice to confront the dice with
     * @return true if the dice is equals to the given dice, false otherwise
     */
    @Override
    public boolean equals(Object dice) {
        return dice instanceof Dice && this.color == ((Dice) dice).color && this.value == ((Dice) dice).value;
    }

    /***
     * Creates a string with the dice information
     * @return a string with the dice information
     */
    @Override
    public String toString(){
        switch (value){
            case 1: return color.escape()+"\u2680"+Color.RESET;
            case 2: return color.escape()+"\u2681"+Color.RESET;
            case 3: return color.escape()+"\u2682"+Color.RESET;
            case 4: return color.escape()+"\u2683"+Color.RESET;
            case 5: return color.escape()+"\u2684"+Color.RESET;
            case 6: return color.escape()+"\u2685"+Color.RESET;
            default: return color.escape()+"N/A"+Color.RESET;
        }
    }
}

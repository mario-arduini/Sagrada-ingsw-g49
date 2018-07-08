package it.polimi.ingsw.model;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import it.polimi.ingsw.model.exceptions.OutOfCardsException;
import it.polimi.ingsw.model.goalcards.*;
import it.polimi.ingsw.model.toolcards.*;
import it.polimi.ingsw.utilities.FilesUtil;

import java.io.BufferedReader;
import java.util.*;

/**
 * Class representing the dealer of the game, handles card shuffleing and extraction, bag of dice and schemas extraction
 */
public class Factory {
    private Stack<BufferedReader> toolCards;
    private List<Color> privateGoalCards;
    private int privateGoalCardsIndex;
    private List<Integer> publicGoalCards;
    private int publicGoalCardsIndex;
    private List<Dice> diceBag;
    private Stack<Schema> schemas;

    private static final int DICE_NUMBER_PER_COLOR = 18;
    private static final int NUMBER_OF_SCHEMAS = 24;
    private static final int NUMBER_OF_TOOL_CARDS = 12;

    /**
     * Create the factory, read toolcards and schemas from file
     */
    public Factory() {
        this.toolCards = new Stack<>();
        List<BufferedReader> files = FilesUtil.listFiles(FilesUtil.TOOL_CARD_FOLDER, NUMBER_OF_TOOL_CARDS);
        for (BufferedReader file:files){
            toolCards.push(file);
        }
        this.privateGoalCards = Arrays.asList(Color.BLUE,Color.GREEN,Color.PURPLE,Color.RED,Color.YELLOW);
        this.privateGoalCardsIndex = 0;
        this.publicGoalCards = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        this.publicGoalCardsIndex = 0;
        this.diceBag = new ArrayList<>();
        Random diceRoller = new Random();
        try {
            for (int i = 0; i < DICE_NUMBER_PER_COLOR; i++) {
                diceBag.add(new Dice(Color.BLUE, diceRoller.nextInt(6) + 1));
                diceBag.add(new Dice(Color.RED, diceRoller.nextInt(6) + 1));
                diceBag.add(new Dice(Color.GREEN, diceRoller.nextInt(6) + 1));
                diceBag.add(new Dice(Color.YELLOW, diceRoller.nextInt(6) + 1));
                diceBag.add(new Dice(Color.PURPLE, diceRoller.nextInt(6) + 1));
            }
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }
        this.schemas = loadSchemasFromFile();

        java.util.Collections.shuffle(toolCards);
        java.util.Collections.shuffle(privateGoalCards);
        java.util.Collections.shuffle(publicGoalCards);
        java.util.Collections.shuffle(diceBag);
        java.util.Collections.shuffle(schemas);


    }

    /**
     * Get the Dice Bag
     * @return List<Dice> representing the dice bag
     */
    List<Dice> getDiceBag() {
        return diceBag;
    }

    /**
     * Extract the desired number of schemas from a previously shuffled Stack
     * @param schemasToExtract Number of schemas to extract
     * @return List of Schemas extracted
     * @throws IndexOutOfBoundsException Signal that there are not enough Schemas left
     */
    List<Schema> extractSchemas(int schemasToExtract) throws IndexOutOfBoundsException {
        Stack<Schema> extracted = new Stack<>();
        for(int i=0;i<schemasToExtract;i++){
            extracted.add(schemas.pop());
        }
        return extracted;
    }

    /**
     * Utility function the loads schemas from files, used internally by the constructor
     * @return Stack of Schemas parsed form files
     */
    private Stack<Schema> loadSchemasFromFile(){
        Stack<Schema> schemas= new Stack<>();
        List<BufferedReader> files = FilesUtil.listFiles(FilesUtil.SCHEMA_FOLDER, NUMBER_OF_SCHEMAS);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject;
        Gson gson = new Gson();

        for (BufferedReader file:files){
            jsonObject = parser.parse(file).getAsJsonObject();
            schemas.push(gson.fromJson(jsonObject, Schema.class));
        }
        return schemas;
    }

    /**
     * Extract a toolcard from the deck
     * @return ToolCard extracted
     * @throws OutOfCardsException signals that all the toolcards have already been extracted
     */
    public ToolCard extractToolCard() throws OutOfCardsException {
        ToolCard toolCard;
        try {
            toolCard = loadToolCardFromFile(toolCards.pop());
        } catch (EmptyStackException e) {
            throw new OutOfCardsException();
        }
        return toolCard;
    }

    /**
     * Utility to load a Toolcard from a file
     * @param file file object as BufferedReader
     * @return ToolCard read
     */
    private ToolCard loadToolCardFromFile(BufferedReader file) {
        ToolCard toolCard;
        JsonParser parser = new JsonParser();
        JsonObject jsonObject;

        jsonObject = parser.parse(file).getAsJsonObject();
        toolCard = new ToolCard(jsonObject);

        return toolCard;
    }

    /**
     * Extract a private goal from the deck
     * @return PrivateGoal extracted
     * @throws OutOfCardsException signals that all the private goals have already been extracted
     */
    public PrivateGoal extractPrivateGoal() throws OutOfCardsException {
        if(privateGoalCardsIndex>=privateGoalCards.size()) throw new OutOfCardsException("Cannot extract Private Goal");
        return new PrivateGoal(privateGoalCards.get(privateGoalCardsIndex++));
    }

    /**
     * Extract a public goal from the deck
     * @return PublicGoal extracted
     * @throws OutOfCardsException signals that all the public goals have already been extracted
     */
    public PublicGoal extractPublicGoal() throws  OutOfCardsException {
        if(publicGoalCardsIndex>=publicGoalCards.size()) throw new OutOfCardsException("Cannot extract Public Goal");
        int index = publicGoalCards.get(publicGoalCardsIndex++);
        PublicGoal pub = null;
        switch (index){
            case 1: pub = new RowColorVariety(); break;
            case 2: pub = new ColumnColorVariety(); break;
            case 3: pub = new RowShadeVariety(); break;
            case 4: pub = new ColumnShadeVariety(); break;
            case 5: pub = new LightShades(); break;
            case 6: pub = new MediumShades(); break;
            case 7: pub = new DarkShades(); break;
            case 8: pub = new FullShadeVariety(); break;
            case 9: pub = new DiagonalColor(); break;
            case 10: pub = new FullColorVariety(); break;
        }
        return pub;
    }

    /**
     * Extract the Draft Pool from the dice bag, given the dimension of the draftpool
     * @param dicesNumber size of the draft pool
     * @return List of Dice representing the draftpool
     * @throws IndexOutOfBoundsException signals that there are not enough Dice left in the bag
     */
    List<Dice> extractPool(int dicesNumber) throws IndexOutOfBoundsException{
        List<Dice> extracted = new ArrayList<Dice>();
        for(int i=0;i<dicesNumber;i++)
            extracted.add( diceBag.remove(diceBag.size()-1) );

        return extracted;
    }

}

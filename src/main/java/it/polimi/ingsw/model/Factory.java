package it.polimi.ingsw.model;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import it.polimi.ingsw.model.exceptions.OutOfCardsException;
import it.polimi.ingsw.model.goalcards.*;
import it.polimi.ingsw.model.toolcards.*;
import it.polimi.ingsw.network.server.Logger;
import it.polimi.ingsw.utilities.FilesUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Factory {
    private Stack<File> toolCards;
    private List<Color> privateGoalCards;
    private int privateGoalCardsIndex;
    private List<Integer> publicGoalCards;
    private int publicGoalCardsIndex;
    private List<Dice> diceBag;
    private Stack<Schema> schemas;

    private static final int DICE_NUMBER_PER_COLOR = 18;

    public Factory() {
        this.toolCards = new Stack<>();
        List<File> files = FilesUtil.listFiles(FilesUtil.TOOLCARD_FOLDER);
        for (File file:files){
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


    public List<Schema> extractSchemas(int schemasToExtract) throws IndexOutOfBoundsException {
        Stack<Schema> extracted = new Stack<>();
        for(int i=0;i<schemasToExtract;i++){
            extracted.add(schemas.pop());
        }
        return extracted;
    }

    private Stack<Schema> loadSchemasFromFile(){
        Stack<Schema> schemas= new Stack<>();
        List<File> files = FilesUtil.listFiles(FilesUtil.SCHEMA_FOLDER);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject;
        Gson gson = new Gson();

        for (File file:files){
            try {
                jsonObject = parser.parse(new FileReader(file)).getAsJsonObject();
                schemas.push(gson.fromJson(jsonObject, Schema.class));
            } catch (FileNotFoundException e) {
                Logger.print(String.format("Schema not found %s", file.getAbsolutePath()));
                e.printStackTrace();
            }
        }
        return schemas;
    }

    public ToolCard extractToolCard() throws OutOfCardsException {
        ToolCard toolCard;
        try {
            toolCard = loadToolCardFromFile(toolCards.pop());
        } catch (FileNotFoundException | EmptyStackException e) {
            throw new OutOfCardsException();
        }
        return toolCard;
    }

    private ToolCard loadToolCardFromFile(File file) throws FileNotFoundException {
        ToolCard toolCard;
        JsonParser parser = new JsonParser();
        JsonObject jsonObject;

        jsonObject = parser.parse(new FileReader(file)).getAsJsonObject();
        toolCard = new ToolCard(jsonObject);

        return toolCard;
    }

    public PrivateGoal extractPrivateGoal() throws OutOfCardsException {
        if(privateGoalCardsIndex>=privateGoalCards.size()) throw new OutOfCardsException("Cannot extract Private Goal");
        return new PrivateGoal(privateGoalCards.get(privateGoalCardsIndex++));
    }

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

    public List<Dice> extractPool(int dicesNumber) throws IndexOutOfBoundsException{
        List<Dice> extracted = new ArrayList<Dice>();
        for(int i=0;i<dicesNumber;i++)
            extracted.add( diceBag.remove(diceBag.size()-1) );

        return extracted;
    }

    public void putInBag(Dice dice){
        dice.roll();
        diceBag.add(new Random().nextInt(diceBag.size()),dice);
    }

    public Dice getFromBag(){
        return diceBag.remove(diceBag.size()-1);
    }

}

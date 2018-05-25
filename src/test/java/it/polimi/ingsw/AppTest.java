package it.polimi.ingsw;

import it.polimi.ingsw.model.toolcards.ToolCard;
import org.junit.jupiter.api.Test;

class FirstJUnitTest{

    @Test
    void test(){

        FactoryTest factoryTest = new FactoryTest();
        FullSetTest fullSetTest = new FullSetTest();
        GameTest gameTest = new GameTest();
        PlayerTest playerTest = new PlayerTest();
        PrivateGoalTest privateGoalTest = new PrivateGoalTest();
        ShadesSetTest shadesSetTest = new ShadesSetTest();
        VarietyTest varietyTest = new VarietyTest();
        WindowTest windowTest = new WindowTest();
        ToolCardsTest toolCardsTest = new ToolCardsTest();

        new DiceTest();
        new ConstraintTest();
        new RoundTest();
        new SchemaTest();
        new WaitingRoomTest();

        factoryTest.extractPrivateTest();
        factoryTest.extractPublicTest();
        factoryTest.extractToolTest();

        fullSetTest.diagonalTest();
        fullSetTest.fullColorTest();
        fullSetTest.fullShadesTest();

        gameTest.testConstructor();
        gameTest.testNextRound();

        playerTest.testConstructor();
        playerTest.testGameplay();

        privateGoalTest.blueTest();
        privateGoalTest.greenTest();
        privateGoalTest.purpleTest();
        privateGoalTest.redTest();
        privateGoalTest.yellowTest();

        shadesSetTest.darkShadesTest();
        shadesSetTest.lightShadesTest();
        shadesSetTest.mediumShadesTest();

        varietyTest.columnColorTest();
        varietyTest.columnShadeTest();
        varietyTest.rowColorTest();
        varietyTest.rowShadeTest();

        windowTest.tesAddDice();
        windowTest.testWindowCostructor();

        toolCardsTest.glazingHammerTest();
    }
}
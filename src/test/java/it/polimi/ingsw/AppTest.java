package it.polimi.ingsw;

import org.junit.jupiter.api.Test;

class FirstJUnitTest{

    @Test
    void test(){
        DiceTest diceTest = new DiceTest();
        ConstraintTest constraintTest = new ConstraintTest();
        FactoryTest factoryTest = new FactoryTest();
        FullSetTest fullSetTest = new FullSetTest();
        GameTest gameTest = new GameTest();
        PlayerTest playerTest = new PlayerTest();
        PrivateGoalTest privateGoalTest = new PrivateGoalTest();
        RoundTest roundTest = new RoundTest();
        SchemaTest schemaTest = new SchemaTest();
        ShadesSetTest shadesSetTest = new ShadesSetTest();
        VarietyTest varietyTest = new VarietyTest();
        WaitingRoomTest waitingRoomTest = new WaitingRoomTest();
        WindowTest windowTest = new WindowTest();

        diceTest.diceTest();

        constraintTest.constraintTest();

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

        roundTest.roundTest();

        schemaTest.schemaTest();

        shadesSetTest.darkShadesTest();
        shadesSetTest.lightShadesTest();
        shadesSetTest.mediumShadesTest();

        varietyTest.columnColorTest();
        varietyTest.columnShadeTest();
        varietyTest.rowColorTest();
        varietyTest.rowShadeTest();

        waitingRoomTest.waitingRoomTest();

        windowTest.tesAddDice();
        windowTest.testWindowCostructor();
    }
}
package it.polimi.ingsw;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.exceptions.InvalidConstraintValueException;
import it.polimi.ingsw.model.exceptions.InvalidDifficultyValueException;
import it.polimi.ingsw.model.exceptions.UnexpectedMatrixSizeException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaTest {

    boolean checkConstraints(Constraint[][] constraint, Schema schema){
        for (int i = 0; i < constraint.length; i++)
            for (int j = 0; j < constraint[i].length; j++)
                if(constraint[i][j] != schema.getConstraint(i,j)) {
                    return false;
                }
        return true;
    }

    @Test
    void schemaTest() {
        Schema schema;
        Constraint[][] constraint;

        try {
            constraint = new Constraint[4][5];
            schema = new Schema(3, constraint);
            assertTrue(schema.getDifficulty() == 3);
            assertTrue(checkConstraints(constraint, schema));
        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

        try {
            constraint = new Constraint[3][5];
            schema = new Schema(3, constraint);
            assertTrue(false);
        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(true);
        }

        try {
            constraint = new Constraint[4][6];
            schema = new Schema(3, constraint);
            assertTrue(false);
        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(true);
        }

        try {
            constraint = new Constraint[4][5];
            schema = new Schema(7, constraint);
            assertTrue(false);
        } catch (InvalidDifficultyValueException e) {
            assertTrue(true);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

        try {
            constraint = new Constraint[4][5];
            schema = new Schema(2, constraint);
            assertTrue(false);
        } catch (InvalidDifficultyValueException e) {
            assertTrue(true);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

        try {
            constraint = new Constraint[4][5];
            constraint[2][3] = new Constraint(Color.RED);
            constraint[0][0] = new Constraint(2);
            constraint[3][4] = new Constraint(Color.PURPLE);
            schema = new Schema(5, constraint);
            assertTrue(checkConstraints(constraint, schema));
        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        } catch (InvalidConstraintValueException e) {
            assertTrue(false);
        }


    }
}

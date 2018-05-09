package it.polimi.ingsw;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.exceptions.InvalidDifficultyValueException;
import it.polimi.ingsw.model.exceptions.UnexpectedMatrixSizeException;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class SchemaTest {

    private boolean checkConstraints(Constraint[][] constraint, Schema schema){
        for (int i = 0; i < constraint.length; i++)
            for (int j = 0; j < constraint[i].length; j++)
                if((constraint[i][j] == null && schema.getConstraint(i,j) != null) ||
                        (constraint[i][j] != null && !constraint[i][j].equals(schema.getConstraint(i,j))))
                    return false;
        return true;
    }

    @Test
    void schemaTest() {
        AtomicReference<Schema> schema = new AtomicReference<>();
        AtomicReference<Constraint[][]> constraint = new AtomicReference<>();
        constraint.getAndSet(new Constraint[4][5]);

        assertDoesNotThrow(() -> schema.getAndSet(new Schema(3, constraint.get())));
        assertEquals(3, schema.get().getDifficulty());
        assertTrue(checkConstraints(constraint.get(), schema.get()));

        constraint.getAndSet(new Constraint[3][5]);
        assertThrows(UnexpectedMatrixSizeException.class, () -> new Schema(3, constraint.get()));

        constraint.getAndSet(new Constraint[4][6]);
        assertThrows(UnexpectedMatrixSizeException.class, () -> new Schema(3, constraint.get()));

        constraint.getAndSet(new Constraint[4][5]);
        assertThrows(InvalidDifficultyValueException.class, () -> new Schema(7, constraint.get()));
        assertThrows(InvalidDifficultyValueException.class, () -> new Schema(2, constraint.get()));

        constraint.get()[2][3] = new Constraint(Color.RED);
        constraint.get()[3][4] = new Constraint(Color.PURPLE);
        assertDoesNotThrow(() -> constraint.get()[0][0] = new Constraint(2));
        assertDoesNotThrow(() -> schema.getAndSet(new Schema(5, constraint.get())));
        assertTrue(checkConstraints(constraint.get(), schema.get()));

        AtomicReference<Schema> schema1 = new AtomicReference<>();
        Constraint[][] constraint1 = new Constraint[4][5];

        assertDoesNotThrow(() -> schema1.getAndSet(new Schema(5, constraint1)));
        assertNotEquals(schema.get(), schema1.get());

        assertDoesNotThrow(() -> schema1.getAndSet(new Schema(4, constraint1)));
        assertNotEquals(schema.get(), schema1.get());

        assertDoesNotThrow(() -> schema1.getAndSet(new Schema(5, constraint.get())));
        assertEquals(schema.get(), schema1.get());

        assertNotEquals(schema.get(), new Object());
    }
}

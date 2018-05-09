package it.polimi.ingsw;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.exceptions.InvalidConstraintValueException;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTest {

    @Test
    void constraintTest(){

        AtomicReference<Constraint> constraint = new AtomicReference<>();
        constraint.getAndSet(new Constraint(Color.RED));
        assertTrue(constraint.get().getColor() == Color.RED && constraint.get().getNumber() == 0);

        assertDoesNotThrow(() -> constraint.getAndSet(new Constraint(5)));
        assertTrue(constraint.get().getColor() == null && constraint.get().getNumber() == 5);

        assertThrows(InvalidConstraintValueException.class, () -> constraint.getAndSet(new Constraint(0)));
        assertThrows(InvalidConstraintValueException.class, () -> constraint.getAndSet(new Constraint(7)));
        assertTrue(constraint.get().getColor() == null && constraint.get().getNumber() == 5);

        AtomicReference<Constraint> constraint1 = new AtomicReference<>();
        AtomicReference<Constraint> constraint2 = new AtomicReference<>();

        constraint1.getAndSet(new Constraint((Color.RED)));
        constraint2.getAndSet(new Constraint((Color.RED)));
        assertEquals(constraint1.get(), constraint2.get());

        constraint2.getAndSet(new Constraint((Color.GREEN)));
        assertNotEquals(constraint1.get(), constraint2.get());

        assertDoesNotThrow(() -> constraint1.getAndSet(new Constraint(5)));
        assertDoesNotThrow(() -> constraint2.getAndSet(new Constraint(5)));
        assertEquals(constraint1.get(), constraint2.get());

        assertDoesNotThrow(() -> constraint2.getAndSet(new Constraint(4)));
        assertNotEquals(constraint1.get(), constraint2.get());

        assertNotEquals(constraint1.get(), new Object());
    }
}

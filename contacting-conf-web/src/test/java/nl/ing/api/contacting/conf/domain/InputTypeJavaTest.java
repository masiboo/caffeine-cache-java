package nl.ing.api.contacting.conf.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputTypeJavaTest {

    @Test
    void testIsValidatableForRadio() {
        assertTrue(InputTypeJava.isValidatable(InputTypeJava.RADIO));
    }

    @Test
    void testIsValidatableForDropdown() {
        assertTrue(InputTypeJava.isValidatable(InputTypeJava.DROPDOWN));
    }

    @Test
    void testIsValidatableForTextbox() {
        assertFalse(InputTypeJava.isValidatable(InputTypeJava.TEXTBOX));
    }
}
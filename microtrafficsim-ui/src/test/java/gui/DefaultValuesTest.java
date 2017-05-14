package gui;

import microtrafficsim.ui.Main;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @author Dominic Parga Cacheiro
 */
public class DefaultValuesTest {
    @Test
    public void areAllValuesNull() {
        assertNull(Main.OPTIONAL_MAP_FILE);
    }
}

package utils.strings;

import microtrafficsim.utils.strings.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestStringUtils {

    @Test
    public void test4With2Digits() {
        assertEquals(" 4", StringUtils.toString(4, 2));
    }

    @Test
    public void test2With3Digits() {
        assertEquals("  2", StringUtils.toString(2, 3));
    }

    @Test
    public void test42With3Digits() {
        assertEquals(" 42", StringUtils.toString(42, 3));
    }

    @Test
    public void test42With1Digit() {
        assertEquals("2", StringUtils.toString(42, 1));
    }
}

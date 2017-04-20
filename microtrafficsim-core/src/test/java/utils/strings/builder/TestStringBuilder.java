package utils.strings.builder;

import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestStringBuilder {

    public static final Logger logger = new EasyMarkableLogger(TestStringBuilder.class);

    @Test
    public void simpleTestLevelStringBuilder() {

        EasyMarkableLogger.setEnabledGlobally(true, true, true, true, true);

        subtestLevelStringBuilder(
                null,
                "....",
                null,
                ",,,,"
        );
        subtestLevelStringBuilder(
                "\n",
                "....",
                "\n",
                ",,,,"
        );
        subtestLevelStringBuilder(
                "\n",
                "....",
                "ups",
                ",,,,"
        );
        subtestLevelStringBuilder(
                "ups",
                "....",
                "ups",
                ",,,,"
        );


        subtestLevelStringBuilder(
                "\n",
                "..",
                "\n",
                ":pljk,"
        );
        subtestLevelStringBuilder(
                "\n",
                "zt",
                "ups",
                "jk"
        );
        subtestLevelStringBuilder(
                "ups",
                "1234",
                "ups",
                "678"
        );
    }

    /**
     * This method uses 'a', 'b', 'c', 'd' and '\n' for testing. '\n' is checked, but the other chars shouldn't be
     * used as {@code separator}. Otherwise, the test fails.
     */
    private void subtestLevelStringBuilder(String innerLevelSeparator, String innerLevelSubString,
                                           String outerLevelSeparator, String outerLevelSubString) {

        logger.debug("innerLevelSeparator = " + innerLevelSeparator);
        logger.debug("innerLevelSubString = " + innerLevelSubString);
        logger.debug("outerLevelSeparator = " + outerLevelSeparator);
        logger.debug("outerLevelSubString = " + outerLevelSubString);

        /* create and fill inner builder */
        LevelStringBuilder innerBuilder = new LevelStringBuilder();
        innerBuilder.setLevelSubString(innerLevelSubString);
        innerBuilder.setLevelSeparator(innerLevelSeparator);
        innerBuilder.incLevel();
        innerBuilder.appendln("a");
        innerBuilder.append("b");
        innerBuilder.append("c");
        innerBuilder.decLevel();
        innerBuilder.setLevel(-1); // sets level to -1, but functionality for negative level is equal to level == 0
        innerBuilder.append("d");

        /* check inner builder */
        String expected = "";
        expected += innerLevelSubString + "a\n";
        expected += innerLevelSubString + "b";
        expected += innerLevelSubString + "cd";
        assertEquals(expected, innerBuilder.toString());
        logger.debug("\n");
        logger.debug("expected = \n" + expected);
        logger.debug("actual   = \n" + innerBuilder.toString());


        /* create and fill outer builder */
        LevelStringBuilder outerBuilder = new LevelStringBuilder();
        outerBuilder.setLevelSubString(outerLevelSubString);
        outerBuilder.setLevelSeparator(outerLevelSeparator);
        outerBuilder.incLevel();
        outerBuilder.append(innerBuilder);

        /* check outer builder */
        expected = "";
        expected += outerLevelSubString + innerLevelSubString + "a\n";
        if (outerLevelSeparator != null)
            if (outerLevelSeparator.contains("\n"))
                expected += outerLevelSubString;
        expected += innerLevelSubString + "b";
        expected += innerLevelSubString + "cd";
        assertEquals(expected, outerBuilder.toString());
        logger.debug("\n");
        logger.debug("expected = \n" + expected);
        logger.debug("actual   = \n" + outerBuilder.toString());

    }
}

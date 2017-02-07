package utils.strings.builder;

import microtrafficsim.utils.strings.builder.LevelStringBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestLevelStringBuilder {

    @Test
    public void simpleTest() {
        LevelStringBuilder stringBuilder = new LevelStringBuilder();

        LevelStringBuilder innerBuilder = new LevelStringBuilder();
        innerBuilder.setLevelSubString("....");
        innerBuilder.incLevel();
        innerBuilder.appendln("a");
        innerBuilder.append("b");
        innerBuilder.append("c");
        innerBuilder.decLevel();
        innerBuilder.setLevel(-1); // sets level to -1, but functionality for negative level is equal to level == 0
        innerBuilder.append("d");

        assertEquals("....a\n....b....cd", innerBuilder.toString());

        LevelStringBuilder outerBuilder = new LevelStringBuilder();
        outerBuilder.setLevelSubString(",,,,");
        outerBuilder.incLevel();
        outerBuilder.append(innerBuilder);

        assertEquals(",,,,....a\n,,,,....b....cd", outerBuilder.toString());
    }
}

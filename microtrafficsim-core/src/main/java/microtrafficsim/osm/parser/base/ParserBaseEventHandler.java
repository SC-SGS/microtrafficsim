package microtrafficsim.osm.parser.base;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Relation;
import microtrafficsim.osm.primitives.Way;


/**
 * Event handler for the {@code ParserBase}.
 *
 * @author Maximilian Luz
 */
public interface ParserBaseEventHandler {

    /**
     * This function is called before the actual parsing begins.
     */
    void onStart();

    /**
     * This function is called after the given stream has been parsed.
     */
    void onEnd();

    /**
     * A callback-function which is called when a {@code Bounds} primitive has
     * been parsed.
     *
     * @param b the {@code Bounds} object which has been parsed.
     */
    void onPrimitiveParsed(Bounds b);

    /**
     * A callback-function which is called when a {@code Node} primitive has
     * been parsed.
     *
     * @param n the {@code Node} object which has been parsed.
     */
    void onPrimitiveParsed(Node n);

    /**
     * A callback-function which is called when a {@code Way} primitive has
     * been parsed.
     *
     * @param w the {@code Way} object which has been parsed.
     */
    void onPrimitiveParsed(Way w);


    /**
     * A callback-function which is called when a {@code Relation} primitive
     * has been parsed.
     *
     * @param r the {@code Relation} object which has been parsed.
     */
    void onPrimitiveParsed(Relation r);
}

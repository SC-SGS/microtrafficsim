package microtrafficsim.osm.parser.ecs.components.traits;

import microtrafficsim.osm.parser.features.streets.ReverseEquals;


/**
 * Interface to indicate that a {@code Component} can be reversed.
 *
 * @author Maximilian Luz
 */
public interface Reversible extends ReverseEquals {

    /**
     * Reverses this component.
     */
    void reverse();
}

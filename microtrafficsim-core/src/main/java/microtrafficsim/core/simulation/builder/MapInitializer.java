package microtrafficsim.core.simulation.builder;

import microtrafficsim.core.logic.StreetGraph;

/**
 * @author Dominic Parga Cacheiro
 */
public interface MapInitializer {

    /**
     * After the {@code StreetGraph} has been created, there are some tasks to be done before the graph can be used.
     *
     * @param protoGraph the generated, raw graph
     * @param seed this parameter can be used for initializing random variables etc.
     * @return the same graph instance as the parameter; just for practical purposes
     */
    StreetGraph postprocessCreatedStreetGraph(StreetGraph protoGraph, long seed);
}

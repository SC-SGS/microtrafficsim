package microtrafficsim.core.simulation.builder;

import microtrafficsim.core.logic.StreetGraph;

/**
 * @author Dominic Parga Cacheiro
 */
public interface MapInitializer {

    /**
     * After the {@code StreetGraph} has been created, there are some tasks to be done before the graph can be used.
     * This includes {@link #postprocessStreetGraph(StreetGraph, long)}. This method can be called twice, but it is
     * not necessary. {@link #postprocessStreetGraph(StreetGraph, long)} should be called to postprocess a already
     * prepared graph.
     *
     * @param protoGraph the generated, raw graph
     * @param seed this parameter can be used for initializing random variables etc.
     * @return the same graph instance as the parameter; just for practical purposes
     */
    StreetGraph postprocessFreshStreetGraph(StreetGraph protoGraph, long seed);

    /**
     * Prepares the graph independent of the fact whether it has just been created or not, e.g. setting seeds. Resets
     * the graph.
     *
     * @param protoGraph the created/reset graph
     * @param seed this parameter can be used for initializing random variables etc.
     * @return the same graph instance as the parameter; just for practical purposes
     *
     * @see #postprocessFreshStreetGraph(StreetGraph, long) postprocessFreshStreetGraph(...) for more information
     */
    StreetGraph postprocessStreetGraph(StreetGraph protoGraph, long seed);
}

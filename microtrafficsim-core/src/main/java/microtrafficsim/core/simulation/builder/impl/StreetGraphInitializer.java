package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.simulation.builder.MapInitializer;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

/**
 * @author Dominic Parga Cacheiro
 */
public class StreetGraphInitializer implements MapInitializer {

    /*
    |====================|
    | (i) MapInitializer |
    |====================|
    */
    @Override
    public StreetGraph postprocessCreatedStreetGraph(StreetGraph protoGraph, long seed) {

        /* init */
        ConcurrentSeedGenerator seedGenerator = new ConcurrentSeedGenerator(seed);

        /* set seeds deterministically */
        for (Node node : protoGraph.getNodes())
            node.setSeed(seedGenerator.next());

        /* prepare crossing logic */
        protoGraph.getNodes().forEach(Node::calculateEdgeIndices);

        return protoGraph;
    }
}

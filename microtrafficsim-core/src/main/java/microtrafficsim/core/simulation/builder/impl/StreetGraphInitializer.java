package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.simulation.builder.MapInitializer;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

/**
 * @author Dominic Parga Cacheiro
 */
public class StreetGraphInitializer implements MapInitializer {

    public StreetGraph postprocessFreshStreetGraph(StreetGraph protoGraph, long seed) {

        /* prepare crossing logic */
        postprocessStreetGraph(protoGraph, seed).getNodes().forEach(Node::calculateEdgeIndices);

        return protoGraph;
    }

    public StreetGraph postprocessStreetGraph(StreetGraph protoGraph, long seed) {

        protoGraph.reset();

        /* init */
        ConcurrentSeedGenerator seedGenerator = new ConcurrentSeedGenerator(seed);

        /* set seeds deterministically */
        for (Node node : protoGraph.getNodes())
            node.setSeed(seedGenerator.next());

        return protoGraph;
    }
}

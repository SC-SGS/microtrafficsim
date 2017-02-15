package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.simulation.builder.MapInitializer;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

/**
 * @author Dominic Parga Cacheiro
 */
public class StreetGraphInitializer implements MapInitializer {

    @Override
    public Graph postprocessFreshGraph(Graph protoGraph, long seed) {

        /* prepare crossing logic */
        postprocessGraph(protoGraph, seed).getNodes().forEach(Node::calculateEdgeIndices);

        return protoGraph;
    }

    @Override
    public Graph postprocessGraph(Graph protoGraph, long seed) {

        protoGraph.reset();

        /* init */
        ConcurrentSeedGenerator seedGenerator = new ConcurrentSeedGenerator(seed);

        /* set seeds deterministically */
        for (Node node : protoGraph.getNodes())
            node.setSeed(seedGenerator.next());

        return protoGraph;
    }
}

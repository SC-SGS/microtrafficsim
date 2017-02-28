package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.simulation.builder.MapInitializer;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

/**
 * @author Dominic Parga Cacheiro
 */
public class StreetGraphInitializer implements MapInitializer {

    /**
     * Calls {@link #postprocessGraph(Graph, long) postprocessGraph(protoGraph, seed)}.
     * {@link StreetGraph#getNodes() getNodes()}.forEach(
     * {@link Node#calculateEdgeIndices() Node::calculateEdgeIndices}
     * )
     */
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

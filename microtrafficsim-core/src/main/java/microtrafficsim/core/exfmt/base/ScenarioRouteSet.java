package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.utils.RouteMatrix;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioRouteSet extends Container.Entry {
    private RouteMatrix.Sparse routes = new RouteMatrix.Sparse();


    /**
     * @param route Takes the route's origin and destination and store the route for this pair
     * @return
     */
    public ArrayList<Long> put(Route<Node> route) {
        Node origin = route.getStart();
        Node destination = route.getEnd();

        HashMap<Long, ArrayList<Long>> tmp = routes.computeIfAbsent(origin.getId(), id -> new HashMap<>());
        ArrayList<Long> edges = new ArrayList<>(route.size());
        for (ShortestPathEdge<?> edge : route) {
            edges.add(edge.getId());
        }
        return tmp.put(destination.getId(), edges);
    }

    public void clear() {
        this.routes.clear();
    }

    public RouteMatrix.Sparse getAll() {
        return this.routes;
    }
}

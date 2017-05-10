package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.utils.RouteMatrix;

import java.util.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioRouteInfo extends Container.Entry {
    private GraphGUID graphGUID;
    HashMap<Integer, Map<Integer, ArrayList<Integer>>> routes = new HashMap<>();


    public GraphGUID getGraphGUID() {
        return graphGUID;
    }

    public void setGraphGUID(GraphGUID graphGUID) {
        this.graphGUID = graphGUID;
    }

    /**
     * @param route Takes the route's origin and destination and store the route for this pair
     * @return output of {@link HashMap#put(Object, Object) HashMap.put(...)}
     */
    public ArrayList<Integer> put(Route<Node> route) {
        Node origin = route.getStart();
        Node destination = route.getEnd();

        Map<Integer, ArrayList<Integer>> tmp = routes.computeIfAbsent(origin.hashCode(), hashcode -> new HashMap<>());
        ArrayList<Integer> edges = new ArrayList<>(route.size());
        for (ShortestPathEdge<?> edge : route) {
            edges.add(edge.hashCode());
        }
        return tmp.put(destination.hashCode(), edges);
    }

    public void set(RouteMatrix routeMatrix) {
        set(RouteMatrix.toSparse(routeMatrix));
    }

    public void set(RouteMatrix.Sparse sparseMatrix) {
        this.graphGUID = sparseMatrix.getGraphGUID();
        routes = new HashMap<>(sparseMatrix);
    }

    public void clearRoutes() {
        graphGUID = null;
        this.routes.clear();
    }

    public HashMap<Integer, Map<Integer, ArrayList<Integer>>> getRoutes() {
        return this.routes;
    }
}

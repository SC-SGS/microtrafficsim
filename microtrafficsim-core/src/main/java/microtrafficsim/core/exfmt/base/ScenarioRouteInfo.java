package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.routes.StackRoute;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;

import java.util.ArrayList;
import java.util.Map;


/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioRouteInfo extends Container.Entry {
    private GraphGUID graphGUID;
    private ArrayList<SparseRoute> sparseRoutes = new ArrayList<>();


    public ScenarioRouteInfo() {}

    public ScenarioRouteInfo(GraphGUID graphGUID, RouteContainer routes) {
        this.graphGUID = graphGUID;
        routes.forEach(this::add);
    }


    public GraphGUID getGraphGUID() {
        return graphGUID;
    }

    public void setGraphGUID(GraphGUID graphGUID) {
        this.graphGUID = graphGUID;
    }


    public RouteContainer toRouteContainer(Graph graph) {
        Map<Integer, Node> nodeMap = graph.getNodeMap();
        Map<Integer, DirectedEdge> edgeMap = graph.getEdgeMap();


        RouteContainer routeContainer = new SortedRouteContainer();

        for (SparseRoute sparseRoute : sparseRoutes) {
            if (MetaRoute.class == sparseRoute.routeClass) {
                Route route = new MetaRoute(
                        nodeMap.get(sparseRoute.originHash),
                        nodeMap.get(sparseRoute.destinationHash),
                        sparseRoute.spawnDelay);

                routeContainer.add(route);
            } else if (StackRoute.class == sparseRoute.routeClass) {
                StackRoute route = new StackRoute(sparseRoute.spawnDelay);
                for (int edgeHash : sparseRoute.edgeHashes) {
                    route.add(edgeMap.get(edgeHash));
                }

                routeContainer.add(route);
            }
        }

        return routeContainer;
    }


    public void add(Route route) {
        SparseRoute sparseRoute = new SparseRoute();

        sparseRoute.routeClass = route.getClass();
        sparseRoute.originHash = route.getOrigin().hashCode();
        sparseRoute.destinationHash = route.getDestination().hashCode();
        sparseRoute.spawnDelay = route.getSpawnDelay();
        for (ShortestPathEdge<Node> edge : route)
            sparseRoute.edgeHashes.add(edge.hashCode());

        sparseRoutes.add(sparseRoute);
    }


    public static class SparseRoute {
        private Class<? extends Route> routeClass;
        private int originHash;
        private int destinationHash;
        private int spawnDelay;
        private ArrayList<Integer> edgeHashes = new ArrayList<>();
    }
}

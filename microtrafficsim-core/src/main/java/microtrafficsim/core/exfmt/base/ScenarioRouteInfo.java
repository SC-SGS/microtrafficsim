package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.routes.StackRoute;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.logic.streets.DirectedEdge;
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
        Map<Node.Key, Node> nodeMap = graph.getNodeMap();
        Map<DirectedEdge.Key, DirectedEdge> edgeMap = graph.getEdgeMap();


        RouteContainer routeContainer = new SortedRouteContainer();

        for (SparseRoute sparseRoute : sparseRoutes) {
            if (MetaRoute.class == sparseRoute.routeClass) {
                Route route = new MetaRoute(
                        nodeMap.get(sparseRoute.originKey),
                        nodeMap.get(sparseRoute.destinationKey),
                        sparseRoute.spawnDelay);

                routeContainer.add(route);
            } else if (StackRoute.class == sparseRoute.routeClass) {
                StackRoute route = new StackRoute(sparseRoute.spawnDelay);
                for (DirectedEdge.Key key: sparseRoute.edgeKeys) {
                    route.add(edgeMap.get(key));
                }

                routeContainer.add(route);
            }
        }

        return routeContainer;
    }


    public void add(Route route) {
        SparseRoute sparseRoute = new SparseRoute();

        sparseRoute.routeClass = route.getClass();
        sparseRoute.originKey = route.getOrigin().key();
        sparseRoute.destinationKey = route.getDestination().key();
        sparseRoute.spawnDelay = route.getSpawnDelay();
        for (DirectedEdge edge : route)
            sparseRoute.edgeKeys.add(edge.key());

        sparseRoutes.add(sparseRoute);
    }


    public static class SparseRoute {
        private Class<? extends Route> routeClass;
        private Node.Key originKey;
        private Node.Key destinationKey;
        private int spawnDelay;
        private ArrayList<DirectedEdge.Key> edgeKeys = new ArrayList<>();
    }
}

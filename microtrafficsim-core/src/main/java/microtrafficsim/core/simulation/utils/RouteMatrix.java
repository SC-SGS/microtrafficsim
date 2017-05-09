package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.builder.RouteIsNotDefinedException;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteMatrix extends HashMap<Node, HashMap<Node, Route<Node>>> {

    private GraphGUID graphGUID;

    public RouteMatrix() {}

    public RouteMatrix(GraphGUID graphGUID) {
        this.graphGUID = graphGUID;
    }

    public GraphGUID getGraphGUID() {
        return graphGUID;
    }

    public void setGraphGUID(GraphGUID graphGUID) {
        this.graphGUID = graphGUID;
    }


    public void add(Route<Node> route) {
        if (route.isEmpty())
            return;

        Node origin = route.getCurrentStart();
        Node destination = route.getEnd();

        if (origin == null || destination == null)
            throw new NullPointerException();

        Map<Node, Route<Node>> tmp = computeIfAbsent(origin, node -> new HashMap<>());
        tmp.put(destination, route);
    }

    /**
     * <p>
     * Stores the route as it is. Example: <br>
     * Let R = A -> B -> C with origin A and destination C. If you remove edge AB from the route, the origin A is
     * still remaining in the route, but the first node is B. In this case, this method {@code addAll} adds B as
     * origin node. Thus the stored routes are only identical to the given ones if untouched.
     *
     * <p>
     * Only exception is the case a vehicle is driving on AB. The route of a vehicle with
     * {@link VehicleState state == SPAWNED} gets the vehicle's current edge added in front. In above example, AB
     * would still be added.
     */
    public void addAll(Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer()) {
            Route<Node> vRoute = vehicle.getDriver().getRoute();
            Route<Node> storedRoute = new Route<>(vRoute.getStart(), vRoute.getEnd());

            vRoute.forEach(storedRoute::push);

            switch (vehicle.getState()) {
                case SPAWNED:
                    storedRoute.push(vehicle.getDirectedEdge());
                case NOT_SPAWNED:
                    add(storedRoute);
            }
        }
    }

    public static RouteMatrix fromSparse(
            Sparse sparseMatrix,
            Map<Integer, Node> nodeLexicon,
            Map<Integer, ? extends ShortestPathEdge<Node>> edgeLexicon) throws RouteIsNotDefinedException {
        RouteMatrix routeMatrix = new RouteMatrix(sparseMatrix.graphGUID);

        /* go over all origin hashcodes */
        boolean isMatrixCorrect = true;
        for (int originHash : sparseMatrix.keySet()) {
            Map<Integer, ArrayList<Integer>> tmp = sparseMatrix.get(originHash);


            /* take origin from node lexicon */
            Node origin = nodeLexicon.get(originHash);
            if (origin == null) {
                isMatrixCorrect = false;
                break;
            }


            /* add new value if not empty after process */
            HashMap<Node, Route<Node>> newValue = new HashMap<>();
            for (int destinationHash : tmp.keySet()) {
                ArrayList<Integer> edgeHashes = tmp.get(destinationHash);


                /* take destination from node lexicon */
                Node destination = nodeLexicon.get(destinationHash);
                if (destination == null) {
                    isMatrixCorrect = false;
                    break;
                }


                /* translate hashcodes into route */
                Route<Node> route = new Route<>(origin, destination);
                for (int edgeHash : edgeHashes) {
                    ShortestPathEdge<Node> nextEdge = edgeLexicon.get(edgeHash);
                    if (nextEdge == null) {
                        isMatrixCorrect = false;
                        break;
                    }
                    route.push(nextEdge);
                }

                if (!isMatrixCorrect)
                    break;
                newValue.put(destination, route);
            }

            if (!isMatrixCorrect)
                break;

            if (!newValue.isEmpty())
                routeMatrix.put(origin, newValue);
        }

        if (!isMatrixCorrect)
            throw new RouteIsNotDefinedException("Some routes could not be assigned due to wrong graph.");

        return routeMatrix;
    }

    public static Sparse toSparse(RouteMatrix routeMatrix) {
        Sparse sparseMatrix = new Sparse(routeMatrix.graphGUID);

        for (Node origin : routeMatrix.keySet()) {
            Map<Node, Route<Node>> tmp = routeMatrix.get(origin);
            for (Node destination : tmp.keySet()) {
                Route<Node> route = tmp.get(destination);

                /* translate route edges into hashcodes */
                ArrayList<Integer> hashList = new ArrayList<>(route.size());
                for (ShortestPathEdge<?> edge : route) {
                    hashList.add(edge.hashCode());
                }

                /* put list of IDs into new matrix */
                Map<Integer, ArrayList<Integer>> tmp2 = sparseMatrix.computeIfAbsent(
                        origin.hashCode(), hash -> new HashMap<>());
                tmp2.put(destination.hashCode(), hashList);
            }
        }
        return sparseMatrix;
    }

    /**
     * The array list storing all IDs is filled by the default iterator of {@link Stack}. Thus you can iterate over
     * the list and add each element to a new stack.
     */
    public static class Sparse extends HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> {

        private GraphGUID graphGUID;

        public Sparse() {}

        public Sparse(GraphGUID graphGUID) {
            this.graphGUID = graphGUID;
        }

        public GraphGUID getGraphGUID() {
            return graphGUID;
        }

        public void setGraphGUID(GraphGUID graphGUID) {
            this.graphGUID = graphGUID;
        }
    }
}

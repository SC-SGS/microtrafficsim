package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.builder.RouteIsNotDefinedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteMatrix extends HashMap<Node, HashMap<Node, Route<Node>>> {

    public static RouteMatrix fromSparse(
            Sparse sparseMatrix,
            HashMap<Long, Node> nodeLexicon,
            HashMap<Long, ? extends ShortestPathEdge<Node>> edgeLexicon) throws RouteIsNotDefinedException {
        RouteMatrix routeMatrix = new RouteMatrix();

        /* go over all origin IDs */
        boolean isMatrixCorrect = true;
        for (long originID : sparseMatrix.keySet()) {
            HashMap<Long, ArrayList<Long>> tmp = sparseMatrix.get(originID);


            /* take origin from node lexicon */
            Node origin = nodeLexicon.get(originID);
            if (origin == null) {
                isMatrixCorrect = false;
                break;
            }


            /* add new value if not empty after process */
            HashMap<Node, Route<Node>> newValue = new HashMap<>();
            for (long destinationID : tmp.keySet()) {
                ArrayList<Long> edgeIDs = tmp.get(destinationID);


                /* take destination from node lexicon */
                Node destination = nodeLexicon.get(destinationID);
                if (destination == null) {
                    isMatrixCorrect = false;
                    break;
                }


                /* translate IDs into route */
                Route<Node> route = new Route<>(origin, destination);
                for (long edgeID : edgeIDs) {
                    ShortestPathEdge<Node> nextEdge = edgeLexicon.get(edgeID);
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
        Sparse sparseMatrix = new Sparse();

        for (Node origin : routeMatrix.keySet()) {
            HashMap<Node, Route<Node>> tmp = routeMatrix.get(origin);
            for (Node destination : tmp.keySet()) {
                Route<Node> route = tmp.get(destination);

                /* translate route edges into IDs */
                ArrayList<Long> idList = new ArrayList<>(route.size());
                for (ShortestPathEdge<?> edge : route) {
                    idList.add(edge.getId());
                }

                /* put list of IDs into new matrix */
                HashMap<Long, ArrayList<Long>> tmp2 = sparseMatrix.computeIfAbsent(
                        origin.getId(), id -> new HashMap<>());
                tmp2.put(destination.getId(), idList);
            }
        }
        return sparseMatrix;
    }

    /**
     * The array list storing all IDs is filled by the default iterator of {@link Stack}. Thus you can iterate over
     * the list and add each element to a new stack.
     */
    public static class Sparse extends HashMap<Long, HashMap<Long, ArrayList<Long>>> {}
}

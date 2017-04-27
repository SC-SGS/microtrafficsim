package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.simulation.utils.RouteMatrix;

import java.util.HashMap;


/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioRouteSet extends Container.Entry {
    private RouteMatrix routes = new RouteMatrix();


    public Route<Node> put(Route<Node> route) {
        Node origin = route.getStart();
        Node destination = route.getEnd();

        HashMap<Node, Route<Node>> tmp = routes.computeIfAbsent(origin, node -> new HashMap<>());
        return tmp.put(destination, route);
    }

    public void clear() {
        this.routes.clear();
    }

    public RouteMatrix getAll() {
        return this.routes;
    }
}

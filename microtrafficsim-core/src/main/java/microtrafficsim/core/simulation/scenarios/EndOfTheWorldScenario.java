package microtrafficsim.core.simulation.scenarios;

import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.map.area.RectangleArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.HaversineDistanceCalculator;

import java.util.function.Supplier;

/**
 * This class defines a concrete scenarios of a simulation. This means it
 * contains an extension of {@link SimulationConfig} to define simulation
 * parameters. Furthermore, this class serves with a list of uniformly randomly
 * chosen start and end nodes for the vehicles and an age that counts the
 * finished simulation steps.
 *
 * @author Dominic Parga Cacheiro, Jan-Oliver Schmidt
 */
public abstract class EndOfTheWorldScenario extends AbstractStartEndScenario {

    // routing
    private final RectangleArea leftEnd, rightEnd, topEnd, bottomEnd;

    /**
     * Standard constructor.
     *
     * @param config The used config file for this scenarios.
     * @param graph

    reetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public EndOfTheWorldScenario(SimulationConfig config, StreetGraph graph,
                                 Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config,
                graph,
                vehicleFactory);
        // routing
        float latLength = Math.min(0.01f, 0.1f * (graph.maxLat - graph.minLat));
        float lonLength = Math.min(0.01f, 0.1f * (graph.maxLon - graph.minLon));
        leftEnd = new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.minLon + lonLength);
        bottomEnd = new RectangleArea(graph.minLat, graph.minLon, graph.minLat + latLength, graph.maxLon);
        rightEnd = new RectangleArea(graph.minLat, graph.maxLon - lonLength, graph.maxLat, graph.maxLon);
        topEnd = new RectangleArea(graph.maxLat - latLength, graph.minLon, graph.maxLat, graph.maxLon);
    }

    @Override
    protected void createNodeFields() {

        // start field
        addStartField(new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.maxLon), 1);

        // end fields
        addEndField(leftEnd, 1);
        addEndField(bottomEnd, 1);
        addEndField(rightEnd, 1);
        addEndField(topEnd, 1);
    }

    @Override
    protected Node[] findRouteNodes() {

        Node start = getRandomStartNode();

        Coordinate center = new Coordinate((graph.maxLat + graph.minLat) / 2, (graph.maxLon + graph.minLon) / 2);
        Coordinate startCoord = start.getCoordinate();
        // if start is below center
        boolean isBottom = center.lat - startCoord.lat > 0;
        boolean isLeft = center.lon - startCoord.lon > 0;
        Area endArea;
        if (isBottom) {
            if (isLeft) {
                // bottom left
                double latDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(bottomEnd.minlat, startCoord.lon));
                double lonDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(startCoord.lat, leftEnd.minlon));
                endArea = latDistance > lonDistance ? leftEnd : bottomEnd;
            } else {
                // bottom right
                double latDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(bottomEnd.minlat, startCoord.lon));
                double lonDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(startCoord.lat, rightEnd.maxlon));
                endArea = latDistance > lonDistance ? rightEnd : bottomEnd;
            }
        } else {
            if (isLeft) {
                // top left
                double latDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(topEnd.maxlat, startCoord.lon));
                double lonDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(startCoord.lat, leftEnd.minlon));
                endArea = latDistance > lonDistance ? leftEnd : topEnd;
            } else {
                // top right
                double latDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(topEnd.maxlat, startCoord.lon));
                double lonDistance = HaversineDistanceCalculator.getDistance(
                        startCoord,
                        new Coordinate(startCoord.lat, rightEnd.maxlon));
                endArea = latDistance > lonDistance ? rightEnd : topEnd;
            }
        }

        return new Node[]{ start, getRandomEndNode(endArea) };
    }
}

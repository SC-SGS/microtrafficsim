package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.vis.scenario.areas.Area;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.random.distributions.impl.Random;

/**
 * <p>
 * Defines one origin and one destination field around the whole graph using its latitude and longitude borders.
 *
 * <p>
 * {@link #getScoutFactory()} returns a bidirectional A-star algorithm.
 *
 * @author Dominic Parga Cacheiro
 */
public class CrossingTheMapScenario extends AreaScenario {

    // matrix
    private final TypedPolygonArea[] destinationAreas;

    public CrossingTheMapScenario(long seed,
                                  SimulationConfig config,
                                  Graph graph) {
        this(new Random(seed), config, graph, new ConcurrentVehicleContainer());
    }

    public CrossingTheMapScenario(Random random,
                                  SimulationConfig config,
                                  Graph graph) {
        this(random, config, graph, new ConcurrentVehicleContainer());
    }

    public CrossingTheMapScenario(long seed,
                                  SimulationConfig config,
                                  Graph graph,
                                  VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    public CrossingTheMapScenario(Random random,
                                  SimulationConfig config,
                                  Graph graph,
                                  VehicleContainer vehicleContainer) {
        super(random, config, graph, vehicleContainer);
        destinationAreas = new TypedPolygonArea[8];


        /* helping variables */
        final Bounds bounds = graph.getBounds();
        double halfLat = (bounds.maxlat - bounds.minlat) / 2;
        double halfLon = (bounds.maxlon - bounds.minlon) / 2;
        double latLength = Math.min(0.01f, 0.1f * (bounds.maxlat - bounds.minlat));
        double lonLength = Math.min(0.01f, 0.1f * (bounds.maxlon - bounds.minlon));


        final Coordinate bottomLeft  = new Coordinate(bounds.minlat, bounds.minlon);
        final Coordinate bottomRight = new Coordinate(bounds.minlat, bounds.maxlon);
        final Coordinate topRight    = new Coordinate(bounds.maxlat, bounds.maxlon);
        final Coordinate topLeft     = new Coordinate(bounds.maxlat, bounds.minlon);

        final Coordinate bottomMid = new Coordinate(bounds.minlat, bounds.minlon + halfLon);
        final Coordinate rightMid  = new Coordinate(bounds.minlat + halfLat, bounds.maxlon);
        final Coordinate topMid    = new Coordinate(bounds.maxlat, bounds.minlon + halfLon);
        final Coordinate leftMid   = new Coordinate(bounds.minlat + halfLat, bounds.minlon);

        final Coordinate innerBottomLeft  = new Coordinate(bounds.minlat + latLength, bounds.minlon + lonLength);
        final Coordinate innerBottomRight = new Coordinate(bounds.minlat + latLength, bounds.maxlon - lonLength);
        final Coordinate innerTopRight    = new Coordinate(bounds.maxlat - latLength, bounds.maxlon - lonLength);
        final Coordinate innerTopLeft     = new Coordinate(bounds.maxlat - latLength, bounds.minlon + lonLength);

        final Coordinate innerBottomMid = new Coordinate(bounds.minlat + latLength, bounds.minlon + halfLon);
        final Coordinate innerRightMid  = new Coordinate(bounds.minlat + halfLat, bounds.maxlon - lonLength);
        final Coordinate innerTopMid    = new Coordinate(bounds.maxlat - latLength, bounds.minlon + halfLon);
        final Coordinate innerLeftMid   = new Coordinate(bounds.minlat + halfLat, bounds.minlon + lonLength);


        /* define areas for filling node lists */
        setDestinationArea(
                Orientation.BOTTOM,
                Orientation.LEFT,
                new TypedPolygonArea(new Coordinate[] {
                        bottomLeft,
                        bottomMid,
                        innerBottomMid,
                        innerBottomLeft
        }, Area.Type.DESTINATION));

        setDestinationArea(
                Orientation.BOTTOM,
                Orientation.RIGHT,
                new TypedPolygonArea(new Coordinate[] {
                        bottomMid,
                        bottomRight,
                        innerBottomRight,
                        innerBottomMid
                }, Area.Type.DESTINATION));

        setDestinationArea(
                Orientation.RIGHT,
                Orientation.BOTTOM,
                new TypedPolygonArea(new Coordinate[] {
                        innerBottomRight,
                        bottomRight,
                        rightMid,
                        innerRightMid
                }, Area.Type.DESTINATION));

        setDestinationArea(
                Orientation.RIGHT,
                Orientation.TOP,
                new TypedPolygonArea(new Coordinate[] {
                        innerRightMid,
                        rightMid,
                        topRight,
                        innerTopRight
                }, Area.Type.DESTINATION));

        setDestinationArea(
                Orientation.TOP,
                Orientation.RIGHT,
                new TypedPolygonArea(new Coordinate[] {
                        innerTopMid,
                        innerTopRight,
                        topRight,
                        topMid
                }, Area.Type.DESTINATION));

        setDestinationArea(
                Orientation.TOP,
                Orientation.LEFT,
                new TypedPolygonArea(new Coordinate[] {
                        innerTopLeft,
                        innerTopMid,
                        topMid,
                        topLeft
                }, Area.Type.DESTINATION));

        setDestinationArea(
                Orientation.LEFT,
                Orientation.TOP,
                new TypedPolygonArea(new Coordinate[] {
                        leftMid,
                        innerLeftMid,
                        innerTopLeft,
                        topLeft
                }, Area.Type.DESTINATION));

        setDestinationArea(
                Orientation.LEFT,
                Orientation.BOTTOM,
                new TypedPolygonArea(new Coordinate[] {
                        bottomLeft,
                        innerBottomLeft,
                        innerLeftMid,
                        leftMid
                }, Area.Type.DESTINATION));

        for (TypedPolygonArea area : destinationAreas)
            getAreaNodeContainer().addArea(area);
    }

    /**
     * <p>
     * Until enough vehicles (defined in {@link SimulationConfig}) are created, this method is doing this:<br>
     * &bull get random origin <br>
     * &bull calculate its position relative to the graph's center <br>
     * &bull get a random destination out of the border field (of nodes) being closest to the chosen origin
     * &bull increase the route count for the found origin-destination-pair
     */
    @Override
    protected void defineRoutesAfterClearing() {
        // note: the directions used in this method's comments are referring to Europe (so the northern hemisphere)

        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            Node origin = getAreaNodeContainer().getRdmOriginNode(getConfig().scenario.nodesAreWeightedUniformly);

            // get end node depending on start node's position
            Graph graph = getGraph();
            final Bounds bounds = graph.getBounds();

            Coordinate center = new Coordinate((bounds.maxlat + bounds.minlat) / 2, (bounds.maxlon + bounds.minlon) / 2);
            Coordinate originCoord = origin.getCoordinate();

            // set data relevant for distance calculation
            Coordinate latProjection = new Coordinate(0, originCoord.lon);
            Coordinate lonProjection = new Coordinate(originCoord.lat, 0);

            Orientation latOrientation, lonOrientation;

            if (center.lat - originCoord.lat > 0) {
                // origin is below from center
                latProjection.lat = bounds.maxlat;
                latOrientation = Orientation.TOP;
            } else {
                // origin is over center
                latProjection.lat = bounds.minlat;
                latOrientation = Orientation.BOTTOM;
            }
            if (center.lon - originCoord.lon > 0) {
                // origin is left from center
                lonProjection.lon = bounds.maxlon;
                lonOrientation = Orientation.RIGHT;
            } else {
                // origin is right from center
                lonProjection.lon = bounds.minlon;
                lonOrientation = Orientation.LEFT;
            }

            double latDistance = HaversineDistanceCalculator.getDistance(originCoord, latProjection);
            double lonDistance = HaversineDistanceCalculator.getDistance(originCoord, lonProjection);
            Node destination;
            if (latDistance > lonDistance)
                destination = getAreaNodeContainer().getRdmNode(getDestinationArea(lonOrientation, latOrientation));
            else
                destination = getAreaNodeContainer().getRdmNode(getDestinationArea(latOrientation, lonOrientation));
            getRoutes().add(new MetaRoute(origin, destination));
        }
    }


    private TypedPolygonArea getDestinationArea(Orientation major, Orientation minor) {
        return destinationAreas[getDestinationAreaIndex(major, minor)];
    }

    private void setDestinationArea(Orientation major, Orientation minor, TypedPolygonArea area) {
        destinationAreas[getDestinationAreaIndex(major, minor)] = area;
    }

    private int getDestinationAreaIndex(Orientation major, Orientation minor) {
        switch (major) {
            case BOTTOM:
                if (minor == Orientation.LEFT)
                    return 0;
                else if (minor == Orientation.RIGHT)
                    return 1;
                else
                    return -1;
            case RIGHT:
                if (minor == Orientation.BOTTOM)
                    return 2;
                else if (minor == Orientation.TOP)
                    return 3;
                else
                    return -1;
            case TOP:
                if (minor == Orientation.RIGHT)
                    return 4;
                else if (minor == Orientation.LEFT)
                    return 5;
                else
                    return -1;
            case LEFT:
                if (minor == Orientation.TOP)
                    return 6;
                else if (minor == Orientation.BOTTOM)
                    return 7;
                else
                    return -1;
            default:
                return -1;
        }
    }


    private enum Orientation {
        LEFT, BOTTOM, RIGHT, TOP
    }
}

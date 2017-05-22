package logic.crossinglogic.automatic;

import logic.crossinglogic.UIValidation;
import logic.crossinglogic.scenarios.pluscrossroad.AbstractPlusCrossroadScenario;
import logic.crossinglogic.scenarios.pluscrossroad.FullPlusCrossroadScenario;
import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.logic.Direction;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.streets.Lane;
import microtrafficsim.core.logic.streets.information.Orientation;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.VehicleSimulation;
import microtrafficsim.math.Geometry;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.collections.FastSortedArrayList;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.id.BasicLongIDGenerator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.logging.LoggingLevel;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestNodeCrossingIndices {
    private static Logger logger = new EasyMarkableLogger(TestNodeCrossingIndices.class);

    @Test
    public void testPlusCrossroad() throws Exception {

        for (int priorityRun = 0; priorityRun < 2; priorityRun++) {

            /* setup config */
            SimulationConfig config = new SimulationConfig();
            AbstractPlusCrossroadScenario.setupConfig(config);
            config.crossingLogic.drivingOnTheRight = priorityRun == 0; // first right-before-left

            /* setup graph */
            File file = new PackagedResource(UIValidation.class, "/logic/validation/plus_crossroad.osm")
                    .asTemporaryFile();
            OSMParser parser = DefaultParserConfig.get(config).build();
            Graph     graph  = parser.parse(file).streetgraph;

            logger.debug("\n" + graph);

            /* setup simulation and scenario */
            AbstractPlusCrossroadScenario scenario = new FullPlusCrossroadScenario(config, graph, null);

            Simulation sim = new VehicleSimulation();
            sim.setAndInitPreparedScenario(scenario);
            boolean hasCheckAnything = false;
            do {
                sim.runOneStep();

                // always 2 vehicles until first one disappears
                if (scenario.getVehicleContainer().getVehicleCount() < 2)
                    continue;

                /* get vehicles */
                int i = 0;
                Vehicle[] vehicles = new Vehicle[2];
                for (Vehicle vehicle : scenario.getVehicleContainer())
                    vehicles[i++] = vehicle;

                /* check only if both are registered in mid */
                if (!scenario.mid.isRegistered(vehicles[0]) || !scenario.mid.isRegistered(vehicles[1]))
                    continue;

                /* get vehicles' position relative to each other */
                Direction direction = Geometry.calcCurveDirection(
                        vehicles[0].getDriver().getRoute().getDestination().getCoordinate(),
                        scenario.mid.getCoordinate(),
                        vehicles[1].getDriver().getRoute().getDestination().getCoordinate());

                /* swap vehicles if priority is left-before-right instead of right-before-left */
                if (!config.crossingLogic.drivingOnTheRight) {
                    Vehicle tmp = vehicles[0];
                    vehicles[0] = vehicles[1];
                    vehicles[1] = tmp;
                }

                /* assert correct priority-to-the-right */
                boolean permissionToV0 = scenario.mid.permissionToCross(vehicles[0]);
                boolean permissionToV1 = scenario.mid.permissionToCross(vehicles[1]);
                boolean anyPermission = permissionToV0 || permissionToV1;
                hasCheckAnything |= anyPermission;
                assertEquals("Incorrect permission to cross the mid node",
                        direction == Direction.LEFT && anyPermission, permissionToV0);
                assertEquals("Incorrect permission to cross the mid node",
                        direction == Direction.RIGHT && anyPermission, permissionToV1);
            } while (scenario.getVehicleContainer().getVehicleCount() > 0);
            assertTrue("Something went wrong with scenario preparation: nothing has been asserted", hasCheckAnything);
        }
    }

    @Test
    public void testLaneIndices() {
        SimulationConfig config = new SimulationConfig();
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.drivingOnTheRight            = true;


        /* define nodes */
        BasicLongIDGenerator idGenerator = new BasicLongIDGenerator();
        Node c  = new Node(idGenerator.next(), new Coordinate( 0,      0),     config.crossingLogic);
        Node bl = new Node(idGenerator.next(), new Coordinate(-0.001, -0.001), config.crossingLogic);
        Node br = new Node(idGenerator.next(), new Coordinate(-0.001,  0.001), config.crossingLogic);
        Node tr = new Node(idGenerator.next(), new Coordinate( 0.001,  0.001), config.crossingLogic);
        Node t  = new Node(idGenerator.next(), new Coordinate( 0.001,  0),     config.crossingLogic);
        Node tl = new Node(idGenerator.next(), new Coordinate( 0.001, -0.001), config.crossingLogic);

        { /* create graph */
            Graph graph = new StreetGraph(new Bounds(bl.getCoordinate(), tr.getCoordinate()));
            float maxVelocity = config.globalMaxVelocity * 3.6f * config.metersPerCell;
            Node[] nodes = new Node[]{
                    c, bl, br, tr, t, tl
            };

            // orientation (>from< or >to< center), second node, lane count
            LinkedList<Triple<Node, Node, Integer>> metaEdges = new LinkedList<>();
            metaEdges.add(new Triple<>( c,  bl, 3 ));
            metaEdges.add(new Triple<>( br, c,  2 ));
            metaEdges.add(new Triple<>( c,  tr, 2 ));
            metaEdges.add(new Triple<>( c,  t,  1 ));
            metaEdges.add(new Triple<>( t,  c,  1 ));
            metaEdges.add(new Triple<>( tl, c,  2 ));


            for (Triple<Node, Node, Integer> triple : metaEdges) {
                Node orig = triple.obj0;
                Node dest = triple.obj1;
                int nLanes = triple.obj2;

                Vec2d vec2d = new Vec2d(dest.getCoordinate().lon - orig.getCoordinate().lon,
                                        dest.getCoordinate().lat - orig.getCoordinate().lat);
                double lengthInMeters = HaversineDistanceCalculator.getDistance(orig.getCoordinate(), dest.getCoordinate());

                DirectedEdge edge = new DirectedEdge(
                        idGenerator.next(),
                        lengthInMeters,
                        vec2d, vec2d,
                        Orientation.FORWARD,
                        orig, dest,
                        new StreetType(StreetType.ROAD),
                        nLanes,
                        maxVelocity,
                        config.metersPerCell,
                        new SimulationConfig.DefaultStreetPriorityFunction()
                );


                orig.addLeavingEdge(edge);
                dest.addIncomingEdge(edge);
                graph.addEdge(edge);
            }

            for (Node node : nodes)
                graph.addNode(node);
            graph.updateGraphGUID();
            graph.getNodes().forEach(Node::updateEdgeIndices);

            // todo remove
            System.err.println("##### ##### ##### ##### ##### ##### ##### ##### ##");
            System.err.println(graph);
            System.err.println("##### ##### ##### ##### ##### ##### ##### ##### ##");
        } /* finish creating graph */


        Map<Lane, Byte> indices = c.calcLaneIndices();
        FastSortedArrayList<Tuple<Byte, Lane>> entries = new FastSortedArrayList<>((t1, t2) -> Byte.compare(t1.obj0, t2.obj0));
        for (Map.Entry<Lane, Byte> entry : indices.entrySet()) {
            entries.add(new Tuple<>(entry.getValue(), entry.getKey()));
        }
        // todo remove
//        entries.forEach(tuple -> System.err.println("idx = " + tuple.obj0 + " : " + tuple.obj1));
    }


    @BeforeClass
    public static void buildSetup() {
        LoggingLevel.setEnabledGlobally(false, false, false, false, false);
    }
}
package logic.crossinglogic;

import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.streets.information.Orientation;
import microtrafficsim.core.map.*;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.Vec2d;

import java.util.HashMap;

/**
 * @author Maximilian Luz
 */
public class MultilaneTestGraph {
    public StreetGraph graph;
    public MapSegment segment;

    public MultilaneTestGraph(SimulationConfig config) {
        Bounds bounds = new Bounds(35.622,  139.7415, 35.634, 139.7450);

        // graph nodes
        Node x   = new Node(0, new Coordinate(35.628, 139.7430), config.crossingLogic);
        Node xtr = new Node(1, new Coordinate(35.629, 139.7445), config.crossingLogic);
        Node xtl = new Node(2, new Coordinate(35.629, 139.7415), config.crossingLogic);
        Node xbr = new Node(3, new Coordinate(35.627, 139.7445), config.crossingLogic);
        Node xbl = new Node(4, new Coordinate(35.627, 139.7415), config.crossingLogic);

        Node tr  = new Node(5, new Coordinate(35.631, 139.7435), config.crossingLogic);
        Node bl  = new Node(6, new Coordinate(35.625, 139.7425), config.crossingLogic);

        Node ct  = new Node(7, new Coordinate(35.630, 139.7433), config.crossingLogic);
        Node cb  = new Node(8, new Coordinate(35.626, 139.7432), config.crossingLogic);

        Node[] nodes = {
                x, xtr, xtl, xbr, xbl, tr, bl, ct, cb
        };

        // map streets
        Coordinate[][] coordinates = {
                // 8
                new Coordinate[] {
                        x.getCoordinate(),
                        xtr.getCoordinate(),
                },
                new Coordinate[]{
                        xtr.getCoordinate(),
                        new Coordinate(35.630, 139.7445),
                        tr.getCoordinate(),
                },
                new Coordinate[]{
                        tr.getCoordinate(),
                        new Coordinate(35.631, 139.7425),
                        new Coordinate(35.630, 139.7415),
                        xtl.getCoordinate(),
                },
                new Coordinate[] {
                        xtl.getCoordinate(),
                        x.getCoordinate(),
                },
                new Coordinate[] {
                        x.getCoordinate(),
                        xbr.getCoordinate(),
                },
                new Coordinate[]{
                        xbr.getCoordinate(),
                        new Coordinate(35.626, 139.7445),
                        new Coordinate(35.625, 139.7435),
                        bl.getCoordinate(),
                },
                new Coordinate[]{
                        bl.getCoordinate(),
                        new Coordinate(35.626, 139.7415),
                        xbl.getCoordinate(),
                },
                new Coordinate[] {
                        xbl.getCoordinate(),
                        x.getCoordinate(),
                },

                // center
                new Coordinate[] {
                        tr.getCoordinate(),
                        ct.getCoordinate(),
                },
                new Coordinate[] {
                        ct.getCoordinate(),
                        cb.getCoordinate(),
                },
                new Coordinate[] {
                        cb.getCoordinate(),
                        bl.getCoordinate(),
                },
        };

        Street[] streets = {
                genStreetFeature(0, coordinates[0], 0.0),
                genStreetFeature(1, coordinates[1], 0.0),
                genStreetFeature(2, coordinates[2], 0.0),
                genStreetFeature(3, coordinates[3], 0.0),
                genStreetFeature(4, coordinates[4], 0.0),
                genStreetFeature(5, coordinates[5], 0.0),
                genStreetFeature(6, coordinates[6], 0.0),
                genStreetFeature(7, coordinates[7], 0.0),

                genStreetFeature(8, coordinates[8], 0.0),
                genStreetFeature(9, coordinates[9], -1.0),
                genStreetFeature(10, coordinates[10], 0.0),
        };

        // graph edges
        StreetEntity x_to_xtr  = genEntity(streets[0],   x, xtr, 2, config);
        StreetEntity xtr_to_tr = genEntity(streets[1], xtr,  tr, 3, config);
        StreetEntity tr_to_xtl = genEntity(streets[2],  tr, xtl, 3, config);
        StreetEntity xtl_to_x  = genEntity(streets[3], xtl,   x, 2, config);
        StreetEntity x_to_xbr  = genEntity(streets[4],   x, xbr, 2, config);
        StreetEntity xbr_to_bl = genEntity(streets[5], xbr,  bl, 3, config);
        StreetEntity bl_to_xbl = genEntity(streets[6],  bl, xbl, 3, config);
        StreetEntity xbl_to_x  = genEntity(streets[7], xbl,   x, 2, config);

        StreetEntity tr_to_ct  = genEntity(streets[8],  tr,  ct, 3, config);
        StreetEntity ct_to_cb  = genEntity(streets[9],  ct,  cb, 3, config);
        StreetEntity cb_to_bl  = genEntity(streets[10], cb,  bl, 3, config);

        StreetEntity[] entities = {
                x_to_xtr,
                xtr_to_tr,
                tr_to_xtl,
                xtl_to_x,
                x_to_xbr,
                xbr_to_bl,
                bl_to_xbl,
                xbl_to_x,
                tr_to_ct,
                ct_to_cb,
                cb_to_bl,
        };

        for (StreetEntity entity : entities) {
            DirectedEdge forward = ((DirectedEdge) entity.getForwardEdge());
            forward.getOrigin().addLeavingEdge(forward);
            forward.getDestination().addIncomingEdge(forward);

            DirectedEdge backward = ((DirectedEdge) entity.getBackwardEdge());
            backward.getOrigin().addLeavingEdge(backward);
            backward.getDestination().addIncomingEdge(backward);
        }

        // set up connectors
        {   // X-crossing
            addConnectorsNToN(xbl_to_x, x_to_xtr, 2);
            addConnectorsNToN(xtl_to_x, x_to_xbr, 2);
            addConnectorsNToN(xtl_to_x, x_to_xtr, 2);
            addConnectorsNToN(xbl_to_x, x_to_xbr, 2);
            addConnectorsNToNrev2(xbl_to_x, xtl_to_x, 2);
            addConnectorsNToNrev1(x_to_xbr, x_to_xtr, 2);
        }

        {   // top T-crossing
            addConnectorsNToN(xtr_to_tr, tr_to_ct, 3);
            addConnectorsNToNrev1(tr_to_xtl, tr_to_ct, 3);
        }

        {   // bottom T-crossing
            addConnectorsNToN(cb_to_bl, bl_to_xbl, 3);
            addConnectorsNToNrev2(cb_to_bl, xbr_to_bl, 3);
        }

        {   // top o of 8, incl. part of T-crossing
            addConnectorsNToN(x_to_xtr, xtr_to_tr, 2);  // 3 to 2
            addConnectorsNToN(xtr_to_tr, tr_to_xtl, 3);
            addConnectorsNToN(tr_to_xtl, xtl_to_x, 2);  // 2 to 3
        }

        {   // bottom o of 8, incl. part of T-crossing
            addConnectorsNToN(x_to_xbr, xbr_to_bl, 2);  // 3 to 2
            addConnectorsNToN(xbr_to_bl, bl_to_xbl, 3);
            addConnectorsNToN(bl_to_xbl, xbl_to_x, 2);  // 2 to 3
        }

        {   // central highway
            addConnectorsNToN(tr_to_ct, ct_to_cb, 3);
            addConnectorsNToN(ct_to_cb, cb_to_bl, 3);
        }


        // add to graph
        graph = new StreetGraph(bounds);
        for (Node node : nodes) {
            graph.addNode(node);
        }

        for (StreetEntity entity : entities) {
            graph.addEdge((DirectedEdge) entity.getForwardEdge());
            graph.addEdge((DirectedEdge) entity.getBackwardEdge());
        }

        // finish graph
        graph.setSeed(config.seed);
        for (Node node : graph.getNodes()) {
            node.updateCrossingIndices();
        }
        graph.updateGraphGUID();

        // map segment
        HashMap<String, Feature<?>> features = new HashMap<>();
        features.put("streets:tertiary", new Feature<>("streets:tertiary", Street.class, streets));

        segment = new MapSegment(bounds, features);
    }


    private static Street genStreetFeature(long id, Coordinate[] coords, double layer) {
        return new Street(id, coords, layer, length(coords), distances(coords));
    }

    private static double length(Coordinate[] coords) {
        double len = 0.0;
        for (int i = 0; i < coords.length - 1; i++)
            len += HaversineDistanceCalculator.getDistance(coords[i], coords[i + 1]);

        return len;
    }

    private static double[] distances(Coordinate[] coords) {
        double[] len = new double[coords.length - 1];
        for (int i = 0; i < coords.length - 1; i++) {
            len[i] = HaversineDistanceCalculator.getDistance(coords[i], coords[i + 1]);
        }

        return len;
    }


    private static StreetEntity genEntity(Street from, Node orig, Node dest, int lanes, SimulationConfig config) {
        float mpc = config.metersPerCell;
        float mv = config.globalMaxVelocity * 3.6f * config.metersPerCell;
        SimulationConfig.StreetPriorityFunction priority = new SimulationConfig.DefaultStreetPriorityFunction();

        Vec2d origDir = new Vec2d(
                from.coordinates[1].lon - from.coordinates[0].lon,
                from.coordinates[1].lat - from.coordinates[0].lat
        );

        Vec2d destDir = new Vec2d(
                from.coordinates[from.coordinates.length - 1].lon - from.coordinates[from.coordinates.length - 1].lon,
                from.coordinates[from.coordinates.length - 2].lat - from.coordinates[from.coordinates.length - 2].lat
        );

        DirectedEdge forward = new DirectedEdge(
                from.id,
                from.length,
                origDir, destDir,
                Orientation.FORWARD,
                orig, dest,
                new StreetType(StreetType.ROAD),
                lanes,
                mv,
                mpc, priority);
        DirectedEdge backward = new DirectedEdge(
                from.id,
                from.length,
                destDir.mul(-1), origDir.mul(-1),
                Orientation.BACKWARD,
                dest, orig,
                new StreetType(StreetType.ROAD),
                lanes,
                mv,
                mpc, priority);

        StreetEntity entity = new StreetEntity(forward, backward, from);
        forward.setEntity(entity);
        backward.setEntity(entity);
        from.setEntity(entity);

        return entity;
    }


    private static void addConnectorsNToN(StreetEntity a, StreetEntity b, int n) {
        DirectedEdge aFwd = (DirectedEdge) a.getForwardEdge();
        DirectedEdge aBwd = (DirectedEdge) a.getBackwardEdge();

        DirectedEdge bFwd = (DirectedEdge) b.getForwardEdge();
        DirectedEdge bBwd = (DirectedEdge) b.getBackwardEdge();

        addConnectorsNToN(aFwd, aBwd, bFwd, bBwd, n);
    }

    private static void addConnectorsNToNrev1(StreetEntity aRev, StreetEntity b, int n) {
        DirectedEdge aBwd = (DirectedEdge) aRev.getBackwardEdge();
        DirectedEdge aFwd = (DirectedEdge) aRev.getForwardEdge();

        DirectedEdge bFwd = (DirectedEdge) b.getForwardEdge();
        DirectedEdge bBwd = (DirectedEdge) b.getBackwardEdge();

        addConnectorsNToN(aBwd, aFwd, bFwd, bBwd, n);
    }

    private static void addConnectorsNToNrev2(StreetEntity a, StreetEntity bRev, int n) {
        DirectedEdge aFwd = (DirectedEdge) a.getForwardEdge();
        DirectedEdge aBwd = (DirectedEdge) a.getBackwardEdge();

        DirectedEdge bBwd = (DirectedEdge) bRev.getBackwardEdge();
        DirectedEdge bFwd = (DirectedEdge) bRev.getForwardEdge();

        addConnectorsNToN(aFwd, aBwd, bBwd, bFwd, n);
    }

    private static void addConnectorsNToN(DirectedEdge aFwd, DirectedEdge aBwd, DirectedEdge bFwd, DirectedEdge bBwd, int n) {
        Node c = aFwd.getDestination();
        if (c != bFwd.getOrigin() || c != aBwd.getOrigin() || c != bBwd.getDestination())
            throw new IllegalArgumentException();

        for (int i = 0; i < n; i++) {
            c.addConnector(aFwd.getLane(i), bFwd.getLane(i));
            c.addConnector(bBwd.getLane(i), aBwd.getLane(i));
        }
    }
}
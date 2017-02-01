package preprocessing.graph;

import microtrafficsim.osm.parser.features.streets.info.OnewayInfo;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.core.parser.processing.Connector;
import microtrafficsim.core.parser.processing.GraphNodeComponent;
import microtrafficsim.core.parser.processing.GraphWayComponent;
import microtrafficsim.core.parser.processing.Ways;
import microtrafficsim.core.parser.processing.Ways.WayLayout;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelation;
import microtrafficsim.osm.primitives.Primitive;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.collections.ArrayUtils;
import microtrafficsim.utils.collections.ArrayUtils.LongestCommonSubstring;
import microtrafficsim.utils.collections.MultiSet;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.BeforeClass;
import org.junit.Test;
import preprocessing.graph.testutils.TestData;
import preprocessing.graph.testutils.TestNodeComponent;
import preprocessing.graph.testutils.mapping.NodeConnectionComponent;
import preprocessing.graph.testutils.mapping.WayMappingComponent;
import preprocessing.graph.testutils.mapping.WaySlice;
import preprocessing.graph.testutils.mapping.WaySliceMapping;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static org.junit.Assert.*;


/**
 * Tests for the StreetGraph unification process.
 * <p>
 * As it is almost impossible to consider Connectors when detecting matches
 * between different DataSets, some of the tests
 * (stage2WayNetworkCompletenessTest, stage2WayNetworkConsistencyTest) below
 * will fail on DataSets with doubled geometry that can not be merged because of
 * different Connectors. (If there are different Connectors on doubled ways,
 * these cannot be merged, thus creating a non-unique mapping of the DataSet
 * before unification to the the DataSet after unification).
 * <p>
 * These tests should not fail if the {@code SG_WAY_MATCHER}-Predicate is sane
 * (i.e. does exclude areas, ...). If the test-file contains doubled geometry
 * that can not (validly) be merged (or differentiated), the the test-file is
 * considered erroneous, otherwise this may be a bug (either in the tests or the
 * parser).
 * <p>
 * The mapping tests should probably be rewritten, a few things could be made easier.
 *
 * @author Maximilian Luz
 */
public class GraphConsistencyTest {
    /*
     * Warning: Here be dragons. Pshhh, they are sleeping, do not wake them,
     * they will hurt you... badly...
     */

    /**
     * If not null, this file is used instead of the default 'map.processing'.
     */
    private static final File OPTIONAL_TEST_FILE = null;

    /**
     * Comparator to order {@code WaySliceMappings} by their {@code from.start} index
     */
    private static final Comparator<WaySliceMapping> CMP_WAYSLICE_MAPPING = (a, b) -> {
        if (a.from.start < b.from.start) return -1;
        if (a.from.start > b.from.start)
            return 1;
        else
            return 0;
    };

    /**
     * Predicate to select the Ways which should be part of the StreetGraph.
     */
    private static final Predicate<Way> SG_WAY_MATCHER = w
            -> w.visible && w.tags.get("highway") != null && !w.tags.get("highway").equals("elevator")
                       && ((w.tags.get("area") == null || !w.tags.get("area").equals("yes")));

    /**
     * The extracted test-data.
     */
    private static TestData testdata = null;


    /**
     * Load, parse and initialize the test data.
     *
     * @throws IOException        relayed from parser
     * @throws XMLStreamException relayed from parser
     * @throws Exception          relayed from parser
     */
    @BeforeClass
    public static void initializeTestData() throws Exception {
        File osmxml;

        //noinspection ConstantConditions
        if (OPTIONAL_TEST_FILE == null)
            osmxml = new PackagedResource(GraphConsistencyTest.class, "/preprocessing/graph/map.osm").asTemporaryFile();
        else
            osmxml = OPTIONAL_TEST_FILE;

        testdata = TestData.parse(osmxml, SG_WAY_MATCHER);

        // setup DataSet for tests
        setupTestNodeComponents(testdata.stage1);
        setupTestNodeComponents(testdata.stage2);
        generateGraphMapping(testdata.stage1, testdata.stage2);
        generateGraphMapping(testdata.stage2, testdata.stage1);
    }

    /**
     * Sets up the {@code TestNodeComponents} on the specified {@code DataSet}.
     *
     * @param dataset the {@code DataSet} on which the {@code TestNodeComponents}
     *                should be set up.
     */
    private static void setupTestNodeComponents(DataSet dataset) {

        // set up way-intersection info
        for (WayEntity way : dataset.ways.values()) {
            if (!way.features.contains(testdata.streetgraph)) continue;

            for (long ref : way.nodes) {
                NodeEntity        node = dataset.nodes.get(ref);
                TestNodeComponent tnc  = node.get(TestNodeComponent.class);

                if (tnc == null) {
                    tnc = new TestNodeComponent(node);
                    node.set(TestNodeComponent.class, tnc);
                }

                tnc.ways.add(way);
            }
        }

        // set up restrictions
        for (RestrictionRelation restriction : dataset.relations.getAll(RestrictionRelation.class).values()) {
            if (restriction.viaType != Primitive.Type.NODE) continue;

            TestNodeComponent tnc = dataset.nodes.get(restriction.via.iterator().next()).get(TestNodeComponent.class);
            if (tnc != null) tnc.restrictions.add(restriction);
        }
    }

    /**
     * Generates a Mapping from {@code from} to {@code to} and stores it in the
     * {@code WayMappingComponents} on {@code from}.
     *
     * @param from the {@code DataSet} from which the mapping should originate.
     * @param to   the {@code DataSet} to which the mapping maps {@code from}.
     */
    private static void generateGraphMapping(DataSet from, DataSet to) {
        // set up temporary NodeConnectionComponents
        for (WayEntity way : to.ways.values()) {
            for (long ref : way.nodes) {
                NodeEntity              node = to.nodes.get(ref);
                NodeConnectionComponent ncc  = node.get(NodeConnectionComponent.class);

                if (ncc == null) {
                    ncc = new NodeConnectionComponent(node);
                    node.set(NodeConnectionComponent.class, ncc);
                }

                ncc.ways.add(way);
            }
        }

        // generate mapping
        for (WayEntity way : from.ways.values()) {
            ArrayList<WaySliceMapping> mapping = getWayMappingParts(way, to);
            way.set(WayMappingComponent.class, new WayMappingComponent(way, mapping));
        }

        // remove temporary NodeConnectionComponents
        for (NodeEntity node : to.nodes.values()) {
            node.remove(NodeConnectionComponent.class);
        }
    }


    /**
     * Get all {@code WaySliceMappings} which are parts of the mapping from {@code
     * from} to {@code to}.
     *
     * @param from the {@code WayEntity} from which the mapping should map.
     * @param to   the {@code DataSet} to which the mapping should map.
     * @return all {@code WaySliceMappings} which are part of the mapping from
     * {@code from} to {@code to}.
     */
    private static ArrayList<WaySliceMapping> getWayMappingParts(WayEntity from, DataSet to) {
        ArrayList<WaySliceMapping> slices = new ArrayList<>();
        HashSet<WayEntity>         ways   = new HashSet<>();

        // get all ways that have something to do with 'from'
        for (long ref : from.nodes)
            ways.addAll(to.nodes.get(ref).get(NodeConnectionComponent.class).ways);

        // generate slices
        for (WayEntity way : ways) {
            slices.addAll(getMatches(from, way));
            slices.addAll(getReverseMatches(from, way));
        }

        slices.sort(CMP_WAYSLICE_MAPPING);
        return slices;
    }

    /**
     * Return all (non-overlapping, longest) {@code WaySliceMappings} mapping directly from
     * some part of {@code from} to some part of {@code to}.
     *
     * @param from the {@code WayEntity} from which the slices should map.
     * @param to   the {@code WayEntity} to which the slices should map.
     * @return all valid {@code WaySliceMappings} from {@code from} to {@code to}.
     */
    private static ArrayList<WaySliceMapping> getMatches(WayEntity from, WayEntity to) {
        ArrayList<WaySliceMapping> matches = new ArrayList<>();

        // compare features
        if (!from.features.equals(to.features)) return matches;

        // compare StreetComponents
        StreetComponent scf = from.get(StreetComponent.class);
        StreetComponent sct = to.get(StreetComponent.class);
        if ((scf == null) != (sct == null)) return matches;
        if (scf != null && !from.get(StreetComponent.class).equals(to.get(StreetComponent.class))) return matches;

        /*
         *  Note: cannot compare other components here as they may have been affected
         *  by merging and splitting.
         */

        // get the common substring
        LongestCommonSubstring lcs = ArrayUtils.longestCommonSubstring(from.nodes, to.nodes);
        if (lcs == null || lcs.length < 2) return matches;

        WaySlice slcf = new WaySlice(from, lcs.startA, lcs.startA + lcs.length - 1);
        WaySlice slct = new WaySlice(to, lcs.startB, lcs.startB + lcs.length - 1);
        matches.add(new WaySliceMapping(slcf, slct));

        // if this is a mapping at the start of from, it might match at the end too
        if (slcf.start == 0 && slct.start > 0) {
            long[] nfrom = Arrays.copyOfRange(from.nodes, slcf.end, from.nodes.length);
            long[] nto   = Arrays.copyOfRange(to.nodes, 0, slct.start + 1);

            lcs = ArrayUtils.longestCommonSubstring(nfrom, nto);

            if (lcs != null && lcs.length >= 2) {
                int startA = lcs.startA + slcf.end;

                slcf = new WaySlice(from, startA, startA + lcs.length - 1);
                slct = new WaySlice(to, lcs.startB, lcs.startB + lcs.length - 1);
                matches.add(new WaySliceMapping(slcf, slct));
            }

            // if this is a mapping at the end of from, it might match at the start to
        } else if (slcf.end == from.nodes.length - 1 && slct.end < to.nodes.length - 1) {
            long[] nfrom = Arrays.copyOfRange(from.nodes, 0, slcf.start + 1);
            long[] nto   = Arrays.copyOfRange(to.nodes, slct.end, to.nodes.length);

            lcs = ArrayUtils.longestCommonSubstring(nfrom, nto);

            if (lcs != null && lcs.length >= 2) {
                int startB = lcs.startB + slct.end;

                slcf = new WaySlice(from, lcs.startA, lcs.startA + lcs.length - 1);
                slct = new WaySlice(to, startB, startB + lcs.length - 1);
                matches.add(new WaySliceMapping(slcf, slct));
            }
        }

        return matches;
    }

    /**
     * Return all (non-overlapping, longest) {@code WaySliceMappings} mapping from
     * some part of {@code from} to some part of reversed {@code to}.
     *
     * @param from the {@code WayEntity} from which the slices should map.
     * @param to   the {@code WayEntity} to which the slices should map.
     * @return all valid {@code WaySliceMappings} from {@code from} to reversed {@code to}.
     */
    private static ArrayList<WaySliceMapping> getReverseMatches(WayEntity from, WayEntity to) {
        ArrayList<WaySliceMapping> matches = new ArrayList<>();

        // compare features
        if (!from.features.equals(to.features)) return matches;

        // compare StreetComponents
        StreetComponent scf = from.get(StreetComponent.class);
        StreetComponent sct = to.get(StreetComponent.class);
        if ((scf == null) != (sct == null)) return matches;
        if (scf != null && !from.get(StreetComponent.class).reverseEquals(to.get(StreetComponent.class)))
            return matches;

        /*
         *  Note: cannot compare other components here as they may have been affected
         *  by merging and splitting.
         */

        // get the common substring
        long[] revto = new long[to.nodes.length];
        for (int i   = 0; i < to.nodes.length; i++)
            revto[i] = to.nodes[to.nodes.length - 1 - i];

        LongestCommonSubstring lcs = ArrayUtils.longestCommonSubstring(from.nodes, revto);
        if (lcs == null || lcs.length < 2) return matches;

        WaySlice slcf = new WaySlice(from, lcs.startA, lcs.startA + lcs.length - 1);
        WaySlice slct = new WaySlice(to, lcs.startB + lcs.length - 1, lcs.startB);
        matches.add(new WaySliceMapping(slcf, slct));

        // if this is a mapping at the start of from, it might match at the end too
        if (slcf.start == 0 && slcf.end > 0) {
            long[] nfrom = Arrays.copyOfRange(from.nodes, slcf.end, from.nodes.length);
            long[] nto   = Arrays.copyOfRange(revto, 0, slct.end + 1);

            lcs = ArrayUtils.longestCommonSubstring(nfrom, nto);

            if (lcs != null && lcs.length >= 2) {
                int startA = lcs.startA + slcf.end;

                slcf = new WaySlice(from, startA, startA + lcs.length - 1);
                slct = new WaySlice(to, lcs.startB + lcs.length - 1, lcs.startB);
                matches.add(new WaySliceMapping(slcf, slct));
            }

            // if this is a mapping at the end of from, it might match at the start to
        } else if (slcf.end == from.nodes.length - 1 && slct.start < to.nodes.length - 1) {
            long[] nfrom = Arrays.copyOfRange(from.nodes, 0, slcf.start + 1);
            long[] nto   = Arrays.copyOfRange(revto, slct.end, to.nodes.length);

            lcs = ArrayUtils.longestCommonSubstring(nfrom, nto);

            if (lcs != null && lcs.length >= 2) {
                int startB = lcs.startB + slcf.end;

                slcf = new WaySlice(from, lcs.startA, lcs.startA + lcs.length - 1);
                slct = new WaySlice(to, startB + lcs.length - 1, startB);
                matches.add(new WaySliceMapping(slcf, slct));
            }
        }

        return matches;
    }


    /**
     * Tests if the connection from {@code from} via {@code via} to {@code to} is
     * restricted by a restriction contained in the given set.
     *
     * @param restrictions the set of restrictions in which to search for.
     * @param via          the id of the node via which the connection runs.
     * @param from         the id of the way from which the connection originates.
     * @param to           the id of the way to which the connection runs.
     * @return {@code true} if {@code restrictions} contains a restriction which
     * can be applied on the specified connection.
     */
    private static boolean streetConnectionRestricted(HashSet<RestrictionRelation> restrictions, long via, long from,
                                                      long to) {

        return restrictions.stream().anyMatch(r -> {
            if (r.viaType != Primitive.Type.NODE) return false;

            if (r.restriction.isOnlyType())    // 'only'-type
                return r.via.get(0) == via && r.from.contains(from) && !r.to.contains(to);
            else    // 'no'-type
                return r.via.get(0) == via && r.from.contains(from) && r.to.contains(to);
        });
    }

    /**
     * Find and return all mappings which can be created by aligning the given
     * {@code slices} differently. It is assumed that {@code slice.from.way} is the
     * same for each slice.
     *
     * @param offset the offset at which the mapping should start.
     * @param last   the end at which the mapping should stop.
     * @param slices the {@code WaySliceMappings} mapping all from the same way.
     * @param stack  the partial mapping to the {@code offset}-index.
     * @param result the resulting mappings, found mappings are appended here.
     */
    private static void getAllMappingsRecursive(int offset, int last, HashSet<WaySliceMapping> slices,
                                                LinkedList<WaySliceMapping>           stack,
                                                ArrayList<ArrayList<WaySliceMapping>> result) {

        // found a mapping, add it ro result
        if (offset == last) {
            ArrayList<WaySliceMapping> mapping = new ArrayList<>(stack);
            Collections.reverse(mapping);
            result.add(mapping);
            stack.pop();
            return;
        }

        // get next part of the mapping
        for (WaySliceMapping slice : slices) {
            if (slice.from.start == offset) {
                HashSet<WaySliceMapping>    nslices = new HashSet<>(slices);
                LinkedList<WaySliceMapping> nstack  = new LinkedList<>(stack);

                nslices.remove(slice);
                nstack.push(slice);

                getAllMappingsRecursive(slice.from.end, last, nslices, nstack, result);
            }
        }

        if (!stack.isEmpty()) stack.pop();
    }

    /**
     * Find and return all Nodes on which the {@code WaySliceMappings} {@code a}
     * and {@code b} intersect.
     *
     * @param a the first {@code WaySliceMapping}.
     * @param b the second {@code WaySliceMapping}.
     * @return all intersection-nodes of {@code a} and {@code b} as references.
     */
    private static ArrayList<Long> getIntersectionNodes(WaySliceMapping a, WaySliceMapping b) {
        ArrayList<Long> intersections = new ArrayList<>();

        for (int i = a.from.start; i <= a.from.end; i++)
            for (int j = b.from.start; j <= b.from.end; j++)
                if (a.from.way.nodes[i] == b.from.way.nodes[j]) intersections.add(a.from.way.nodes[i]);

        return intersections;
    }

    /**
     * Tests if the given set contains the {@code Connector} described by the given
     * IDs.
     *
     * @param set  the set in which to look for the {@code Connector}.
     * @param via  the id of the node via which the {@code Connector} runs.
     * @param from the id of the way from which the {@code Connector} originates.
     * @param to   the id of the way to which the {@code Connector} runs.
     * @return {@code true} if {@code set} contains a {@code Connector} which can
     * be described by the given IDs.
     */
    private static boolean containsConnectorByIDs(HashSet<Connector> set, long via, long from, long to) {
        for (Connector e : set)
            if (e.via.id == via && e.from.id == from && e.to.id == to) return true;

        return false;
    }


    /**
     * Check that all entities in the given {@code DataSet} have sane values.
     *
     * @param dataset the {@code DataSet} to check.
     */
    private static void assertDataSetConsistent(DataSet dataset) {

        // consistency check for NodeEntities
        for (NodeEntity node : dataset.nodes.values()) {
            // check component entity references
            for (Component c : node.getAll().values()) {
                assertTrue(c.getEntity() == node);
            }
        }

        // consistency check for WayEntities
        for (WayEntity way : dataset.ways.values()) {
            StreetComponent   streetcomp = way.get(StreetComponent.class);
            GraphWayComponent gwc        = way.get(GraphWayComponent.class);

            // check component entity references
            for (Component c : way.getAll().values()) {
                assertTrue(c.getEntity() == way);
            }

            // way must contain at least 2 nodes
            assertTrue(way.nodes.length > 1);

            // succeeding nodes must be different
            for (int i = 1; i < way.nodes.length; i++)
                assertTrue(way.nodes[i - 1] != way.nodes[i]);

            // if way is part of StreetGraph, there must be a StreetComponent
            if (way.features.contains(testdata.streetgraph)) assertNotNull(streetcomp);

            // check values of StreetComponent, if it exists
            if (streetcomp != null) {
                // oneway is always valid

                // check lanes and maxspeed
                if (streetcomp.oneway == OnewayInfo.NO) {
                    assertTrue(streetcomp.lanes.forward > 0);
                    assertTrue(streetcomp.lanes.backward > 0);
                    assertTrue(streetcomp.maxspeed.forward > 0);
                    assertTrue(streetcomp.maxspeed.backward > 0);

                } else if (streetcomp.oneway == OnewayInfo.FORWARD || streetcomp.oneway == OnewayInfo.REVERSIBLE) {
                    assertTrue(streetcomp.lanes.forward > 0);
                    assertTrue(streetcomp.lanes.backward == 0);
                    assertTrue(streetcomp.maxspeed.forward > 0);
                    assertTrue(streetcomp.maxspeed.backward == 0);

                } else if (streetcomp.oneway == OnewayInfo.BACKWARD) {
                    assertTrue(streetcomp.lanes.forward == 0);
                    assertTrue(streetcomp.lanes.backward > 0);
                    assertTrue(streetcomp.maxspeed.forward == 0);
                    assertTrue(streetcomp.maxspeed.backward > 0);
                }

                // lanes.sum must be sum of lanes.forward and lanes.backward
                assertTrue(streetcomp.lanes.sum == streetcomp.lanes.forward + streetcomp.lanes.backward);
            }

            // check connectors if GraphWayComponent is present
            if (gwc != null) {
                for (Connector c : gwc.from) {
                    GraphWayComponent gwcTo = dataset.ways.get(c.to.id).get(GraphWayComponent.class);
                    assertNotNull(gwcTo);
                    assertTrue(gwcTo.to.contains(c));
                }

                for (Connector c : gwc.from) {
                    GraphWayComponent gwcFrom = dataset.ways.get(c.from.id).get(GraphWayComponent.class);
                    assertNotNull(gwcFrom);
                    assertTrue(gwcFrom.from.contains(c));
                }
            }
        }

        // consistency check for RestrictionRelations
        for (RestrictionRelation r : dataset.relations.getAll(RestrictionRelation.class).values()) {

            // check if viaType is valid (NODE and via.size() == 1 or WAY and via.size() >= 1)
            if (r.viaType == Primitive.Type.NODE) {
                assertTrue(r.via.size() == 1);
            } else if (r.viaType == Primitive.Type.WAY) {
                assertTrue(r.via.size() >= 1);
            } else {
                fail("RestrictionRelation.viaType must be either NODE or WAY");
            }

            // assert that all referenced entities  are contained in dataset
            r.from.forEach((Long id) -> assertNotNull(dataset.ways.get(id)));
            r.to.forEach((Long id) -> assertNotNull(dataset.ways.get(id)));
            if (r.viaType == Primitive.Type.NODE)
                r.via.forEach((Long id) -> assertNotNull(dataset.nodes.get(id)));
            else
                r.via.forEach((Long id) -> assertNotNull(dataset.ways.get(id)));
        }
    }

    /**
     * Check that the given {@code mapping} is valid for the given {@code way}.
     *
     * @param mapping the mapping mapping from {@code way} to other {@code WayEntities}.
     * @param way     the {@code way} from which the mapping should map.
     */
    private static void assertWayMappingValid(ArrayList<WaySliceMapping> mapping, WayEntity way) {
        int last = 0;
        for (WaySliceMapping slice : mapping) {
            assertEquals("mapping does not align or is not unique", last, slice.from.start);
            assertTrue("invalid mapping", way == slice.from.way);

            if (slice.to.start < slice.to.end) {
                int len = slice.to.start - slice.to.end + 1;

                long[] to   = slice.to.way.nodes;
                long[] from = slice.from.way.nodes;

                for (int i = 0; i < len; i++)
                    assertEquals("invalid mapping", from[slice.from.start + i], to[slice.to.start + i]);

            } else {
                int len = slice.to.end - slice.to.start + 1;

                long[] to   = slice.to.way.nodes;
                long[] from = slice.from.way.nodes;

                for (int i = 0; i < len; i++)
                    assertEquals("invalid mapping", from[slice.from.start + i], to[slice.to.end - i]);
            }

            last = slice.from.end;
        }

        assertEquals(way.nodes.length - 1, last);
    }

    /**
     * Check that for the given mapping all street-connections on {@code from},
     * either through {@code Connectors} or implied through other properties, are
     * also available on {@code to}.
     *
     * @param from    the {@code DataSet} used as reference.
     * @param to      the {@code DataSet} on which the {@code Connectors} are checked.
     * @param mapping a mapping from a way in {@code from} to {@code to} on which
     *                the {@code Connectors} should be checked.
     */
    private static void assertConnectorPropagation(DataSet from, DataSet to, ArrayList<WaySliceMapping> mapping) {
        WayEntity                way      = mapping.get(0).from.way;
        HashSet<WaySliceMapping> adjacent = new HashSet<>();

        // get all mappings adjacent to the from-way
        {
            // get adjacent ways
            HashSet<WayEntity> ways = new HashSet<>();
            for (long node : way.nodes)
                ways.addAll(from.nodes.get(node).get(TestNodeComponent.class).ways);

            ways.remove(way);

            // get adjacent/intersecting mappings
            for (WayEntity w : ways) {
                for (WaySliceMapping m : w.get(WayMappingComponent.class).mapsto) {
                    boolean contained = false;

                    for (int i = m.from.start; i <= m.from.end; i++) {
                        if (ArrayUtils.contains(way.nodes, w.nodes[i])) {
                            contained = true;
                            break;
                        }
                    }

                    if (contained) adjacent.add(m);
                }
            }
        }

        // check connectors on all slices
        Iterator<WaySliceMapping> iter = mapping.iterator();

        WaySliceMapping prev = null;
        WaySliceMapping curr = iter.next();
        WaySliceMapping next = iter.hasNext() ? iter.next() : null;

        while (curr != null) {
            // make sure connectors prev<-->curr exist on to
            if (prev != null) assertConnectorsExistBetweenSlices(prev, curr);

            // make sure connectors curr<-->next exist on to
            if (next != null) assertConnectorsExistBetweenSlices(curr, next);

            // check connectors between adjacent ways (except prev, next)
            for (WaySliceMapping other : adjacent) {
                ArrayList<Long> intersections = getIntersectionNodes(curr, other);

                for (long x : intersections)
                    assertConnectorsEqualOnIntersection(curr, other, x, from, to);
            }

            prev = curr;
            curr = next;
            next = iter.hasNext() ? iter.next() : null;
        }
    }

    /**
     * Check that all {@code Connectors} between the {@code to.way} of the given
     * slices exist, if they can exist. {@code first} and {@code second} be aligned
     * so that the last node of {@code fist} ist the first node of {@code second}.
     *
     * @param first  the first {@code WaySliceMapping} in sequence.
     * @param second the second {@code WaySliceMapping} in sequence.
     */
    private static void assertConnectorsExistBetweenSlices(WaySliceMapping first, WaySliceMapping second) {
        long via = first.to.way.nodes[first.to.end];
        long fid = first.to.way.id;
        long sid = second.to.way.id;

        assertEquals(via, second.to.way.nodes[second.to.start]);

        GraphWayComponent gwcf = first.to.way.get(GraphWayComponent.class);
        GraphWayComponent gwcs = second.to.way.get(GraphWayComponent.class);
        if (gwcf == null || gwcs == null) return;

        StreetComponent scf = first.to.way.get(StreetComponent.class);
        StreetComponent scs = second.to.way.get(StreetComponent.class);
        if (scf == null || scs == null) return;

        WayLayout layoutFS = Ways.getLayout(via, first.to.way, second.to.way);
        WayLayout layoutSF = Ways.getLayout(via, second.to.way, first.to.way);

        // basic consistency
        assertEquals(containsConnectorByIDs(gwcf.from, via, fid, sid), containsConnectorByIDs(gwcs.to, via, fid, sid));
        assertEquals(containsConnectorByIDs(gwcf.to, via, sid, fid), containsConnectorByIDs(gwcs.from, via, sid, fid));

        // If there can be a connection, there must be connectors.
        // Self-intersections are assumed to be fully interconnected.
        if (fid != sid) {
            if (Ways.isConnectionPossible(layoutFS, scf.oneway, scs.oneway))
                assertTrue(containsConnectorByIDs(gwcf.from, via, fid, sid));

            if (Ways.isConnectionPossible(layoutSF, scs.oneway, scf.oneway))
                assertTrue(containsConnectorByIDs(gwcs.from, via, sid, fid));
        }
    }

    /**
     * Check that all connections between the given slices implied by {@code
     * Connectors} or other properties are equal on {@code slice.from} and {@code
     * slice.to}.
     *
     * @param a           the first {@code WaySliceMapping}
     * @param b           the second {@code WaySliceMapping}
     * @param node        the id of the node via which the connections run.
     * @param datasetFrom the {@code DataSet} corresponding to the {@code
     *                    from}-part of the slices.
     * @param datasetTo   the {@code DataSet} corresponding to the {@code to}-part
     *                    of the slices.
     */
    private static void assertConnectorsEqualOnIntersection(WaySliceMapping a, WaySliceMapping b, long node,
                                                            DataSet datasetFrom, DataSet datasetTo) {

        NodeEntity fvia = datasetFrom.nodes.get(node);
        NodeEntity tvia = datasetTo.nodes.get(node);

        // get and check GraphWayComponents
        GraphWayComponent gwcFromA = a.from.way.get(GraphWayComponent.class);
        GraphWayComponent gwcFromB = b.from.way.get(GraphWayComponent.class);

        GraphWayComponent gwcToA = a.to.way.get(GraphWayComponent.class);
        GraphWayComponent gwcToB = b.to.way.get(GraphWayComponent.class);

        assertEquals(gwcToA != null, gwcFromA != null);
        assertEquals(gwcToB != null, gwcFromB != null);

        if (gwcFromA == null || gwcFromB == null) return;
        assert gwcToA != null;
        assert gwcToB != null;

        if (a.to.way != b.to.way) {
            // get IDs
            long fid  = fvia.id;
            long tid  = tvia.id;
            long afid = a.from.way.id;
            long bfid = b.from.way.id;
            long atid = a.to.way.id;
            long btid = b.to.way.id;

            // basic consistency
            assertEquals(containsConnectorByIDs(gwcFromA.from, fid, afid, bfid),
                         containsConnectorByIDs(gwcFromB.to, fid, afid, bfid));
            assertEquals(containsConnectorByIDs(gwcFromA.to, fid, bfid, afid),
                         containsConnectorByIDs(gwcFromB.from, fid, bfid, afid));
            assertEquals(containsConnectorByIDs(gwcToA.from, tid, atid, btid),
                         containsConnectorByIDs(gwcToB.to, tid, atid, btid));
            assertEquals(containsConnectorByIDs(gwcToA.to, tid, btid, atid),
                         containsConnectorByIDs(gwcToB.from, tid, btid, atid));

            // connector propagation
            boolean possibleFromAB = false;
            boolean possibleFromBA = false;
            boolean possibleToAB   = false;
            boolean possibleToBA   = false;

            StreetComponent scfa = a.from.way.get(StreetComponent.class);
            StreetComponent scfb = b.from.way.get(StreetComponent.class);
            StreetComponent scta = a.to.way.get(StreetComponent.class);
            StreetComponent sctb = b.to.way.get(StreetComponent.class);

            assertEquals(scfa != null, scta != null);
            assertEquals(scfb != null, sctb != null);

            if (scfa != null && scfb != null) {
                assert scta != null;
                assert sctb != null;

                WayLayout layoutFromAB = Ways.getLayout(node, a.from.way, b.from.way);
                WayLayout layoutFromBA = Ways.getLayout(node, b.from.way, a.from.way);
                WayLayout layoutToAB   = Ways.getLayout(node, a.to.way, b.to.way);
                WayLayout layoutToBA   = Ways.getLayout(node, b.to.way, a.to.way);

                possibleFromAB = Ways.isConnectionPossible(layoutFromAB, scfa.oneway, scfb.oneway);
                possibleFromBA = Ways.isConnectionPossible(layoutFromBA, scfb.oneway, scfa.oneway);
                possibleToAB   = Ways.isConnectionPossible(layoutToAB, scta.oneway, sctb.oneway);
                possibleToBA   = Ways.isConnectionPossible(layoutToBA, sctb.oneway, scta.oneway);
            }

            if (possibleFromAB && possibleToAB)
                assertEquals(containsConnectorByIDs(gwcFromA.from, fid, afid, bfid),
                             containsConnectorByIDs(gwcToA.from, tid, atid, btid));

            if (!possibleToAB) assertFalse(containsConnectorByIDs(gwcToA.from, tid, atid, btid));
            if (!possibleFromAB) assertFalse(containsConnectorByIDs(gwcFromA.from, fid, afid, bfid));

            if (possibleFromBA && possibleToBA)
                assertEquals(containsConnectorByIDs(gwcFromA.to, fid, bfid, afid),
                             containsConnectorByIDs(gwcToA.to, tid, btid, atid));

            if (!possibleToBA) assertFalse(containsConnectorByIDs(gwcToA.to, tid, btid, atid));
            if (!possibleFromBA) assertFalse(containsConnectorByIDs(gwcFromA.to, fid, bfid, afid));

            // else if to.way is the same and cyclic
        } else if (a.to.way.nodes[0] == node && a.to.way.nodes[a.to.way.nodes.length - 1] == tvia.id) {
            // get IDs
            long fid  = fvia.id;
            long afid = a.from.way.id;
            long bfid = b.from.way.id;

            boolean toAB;
            boolean toBA;

            int last = b.to.way.nodes.length - 1;
            if ((a.to.start == 0 || a.to.end == 0) && (b.to.start == last || b.to.end == last)) {
                toAB = gwcToA.cyclicStartToEnd;
                toBA = gwcToA.cyclicEndToStart;

            } else if ((a.to.start == last || a.to.end == last) && (b.to.start == 0 || b.to.end == 0)) {
                toAB = gwcToA.cyclicEndToStart;
                toBA = gwcToA.cyclicStartToEnd;

            } else {
                /*
                 * These aren't the Slices you're looking for!
                 * NOTE: this might happen when we are checking from the unified DataSet
                 * against the original DataSet and there is a way which is cyclic AND
                 * intersects with the start/end other other than at its start/end.
                 */
                return;
            }

            // basic consistency
            assertEquals(containsConnectorByIDs(gwcFromA.from, fid, afid, bfid),
                         containsConnectorByIDs(gwcFromB.to, fid, afid, bfid));
            assertEquals(containsConnectorByIDs(gwcFromA.to, fid, bfid, afid),
                         containsConnectorByIDs(gwcFromB.from, fid, bfid, afid));

            // connector propagation
            assertEquals(containsConnectorByIDs(gwcFromA.from, fid, afid, bfid), toAB);
            assertEquals(containsConnectorByIDs(gwcFromA.to, fid, bfid, afid), toBA);

        }
        /* else: intersection inside to.way, all required connectors automatically present */
    }


    /**
     * Check that the {@code DataSet} is consistent before the unification-process
     * starts.
     */
    @Test
    public void stage1DatasetConsistencyTest() {
        assertDataSetConsistent(testdata.stage1);
    }

    /**
     * Check that the {@code GraphNodeComponents} are valid.
     */
    @Test
    public void stage1GraphNodeConsistencyTest() {
        for (NodeEntity node : testdata.stage1.nodes.values()) {
            GraphNodeComponent gnc = node.get(GraphNodeComponent.class);
            TestNodeComponent  tnc = node.get(TestNodeComponent.class);

            // compare 'GraphNodeComponent.ways' with 'TestNodeComponent.ways'
            if (gnc != null && tnc != null) {

                // compare size
                assertEquals(tnc.ways.size(), gnc.ways.size());
                assertEquals(tnc.ways.count(), gnc.ways.count());

                // compare elements
                for (Iterator<MultiSet.Entry<WayEntity>> gIter = gnc.ways.entryIterator(); gIter.hasNext();) {
                    MultiSet.Entry<WayEntity> gEntry = gIter.next();

                    boolean found = false;
                    for (Iterator<MultiSet.Entry<WayEntity>> tIter = gnc.ways.entryIterator(); tIter.hasNext();) {
                        MultiSet.Entry<WayEntity> tEntry = tIter.next();

                        if (gEntry.getCount() == tEntry.getCount()
                            && gEntry.getElement().id == tEntry.getElement().id) {
                            found = true;
                            break;
                        }
                    }

                    assertTrue("TestGraphComponent and GraphNodeComponent differ", found);
                }

            } else if (gnc != null || tnc != null) {
                fail("TestGraphComponent and GraphNodeComponent differ");
            }
        }
    }

    /**
     * Check that the {@code GraphWayComponent} are valid before unification. This
     * includes checking for Connectors.
     */
    @Test
    public void stag1GraphWayConsistencyTest() {
        // NOTE: stage 1 is before unifications, way-IDs are not modified yet

        for (NodeEntity node : testdata.stage1.nodes.values()) {
            TestNodeComponent tnc = node.get(TestNodeComponent.class);
            if (tnc == null) continue;


            for (WayEntity a : tnc.ways) {
                GraphWayComponent gwcA = a.get(GraphWayComponent.class);
                StreetComponent   scA  = a.get(StreetComponent.class);

                for (WayEntity b : tnc.ways) {

                    // there are no restrictions that can be applied to cyclic way connections
                    if (a == b) {
                        if (a.nodes[0] == a.nodes[a.nodes.length - 1]) {
                            if (scA.oneway == OnewayInfo.NO || scA.oneway == OnewayInfo.REVERSIBLE) {
                                assertTrue(gwcA.cyclicEndToStart);
                                assertTrue(gwcA.cyclicStartToEnd);

                            } else if (scA.oneway == OnewayInfo.FORWARD) {
                                assertTrue(gwcA.cyclicEndToStart);
                                assertFalse(gwcA.cyclicStartToEnd);

                            } else if (scA.oneway == OnewayInfo.BACKWARD) {
                                assertFalse(gwcA.cyclicEndToStart);
                                assertTrue(gwcA.cyclicStartToEnd);
                            }
                        }

                        // make sure either there is either a Connector or a Restriction (XOR)
                    } else {
                        GraphWayComponent gwcB = b.get(GraphWayComponent.class);
                        StreetComponent   scB  = b.get(StreetComponent.class);

                        Predicate<Connector> cmatchAB
                                = c -> c.via.id == node.id && c.from.id == a.id && c.to.id == b.id;

                        Predicate<Connector> cmatchBA
                                = c -> c.via.id == node.id && c.from.id == b.id && c.to.id == a.id;

                        // get existence of connectors
                        boolean connectorAtoB   = gwcA.from.stream().anyMatch(cmatchAB);
                        boolean connectorBtoA   = gwcB.from.stream().anyMatch(cmatchBA);
                        boolean connectorAfromB = gwcA.to.stream().anyMatch(cmatchBA);
                        boolean connectorBfromA = gwcB.to.stream().anyMatch(cmatchAB);

                        // get existence of restrictions
                        boolean restrictedAB = streetConnectionRestricted(tnc.restrictions, node.id, a.id, b.id);
                        boolean restrictedBA = streetConnectionRestricted(tnc.restrictions, node.id, b.id, a.id);

                        // get required connectors/restrictions
                        WayLayout layout     = Ways.getLayout(node.id, a, b);
                        assertNotNull(layout);
                        boolean   requiresAB = Ways.isConnectionPossible(layout, scA.oneway, scB.oneway);
                        boolean   requiresBA = Ways.isConnectionPossible(layout.getReversed(), scA.oneway, scB.oneway);

                        // if a Connector is registered on Way x, it has to be registered on Way y
                        assertTrue("inconsistent connector registers", connectorAtoB == connectorBfromA);
                        assertTrue("inconsistent connector registers", connectorBtoA == connectorAfromB);

                        if (requiresAB)
                            assertTrue("there must be either a connector or a restriction",
                                       connectorAtoB != restrictedAB);

                        if (requiresBA)
                            assertTrue("there must be either a connector or a restriction",
                                       connectorBtoA != restrictedBA);
                    }
                }
            }
        }
    }

    /**
     * Check that the {@code DataSet} is consistent after the unification-process.
     */
    @Test
    public void stage2DatasetConsistencyTest() {
        assertDataSetConsistent(testdata.stage1);
    }

    /**
     * Check that the street-graph of the {@code DataSet} after the the
     * unification-process is minimal.
     */
    @Test
    public void stage2GraphMinimalTest() {
        for (NodeEntity node : testdata.stage2.nodes.values()) {
            TestNodeComponent tnc = node.get(TestNodeComponent.class);
            if (tnc == null) continue;

            if (tnc.ways.size() == 2 && tnc.ways.count() == 2) {
                Iterator<WayEntity> it = tnc.ways.iterator();
                WayEntity           w1 = it.next();
                WayEntity           w2 = it.next();

                assertNull("way segments separate but should be merged",
                           Ways.mergepoint(testdata.stage2, node, w1, w2));
            }
        }
    }

    /**
     * Check that the ways in the street-graph are split on every intersection.
     */
    @Test
    public void stage2GraphIntersectionsTest() {
        for (NodeEntity node : testdata.stage2.nodes.values()) {
            TestNodeComponent tnc = node.get(TestNodeComponent.class);
            if (tnc == null) continue;

            int size   = tnc.ways.size();
            int count  = tnc.ways.count();
            int cyclic = 0;

            // count cyclic ways
            for (WayEntity way : tnc.ways)
                if (node.id == way.nodes[0] && node.id == way.nodes[way.nodes.length - 1])
                    cyclic++;

            assertEquals("invalid split point", size, count - cyclic);
        }
    }

    /**
     * Check that every way in stage1 can be represented by a set of unique ways in
     * stage 2 (unique mapping). Also tests that the Connectors are successfully
     * propagated from stage 1 to stage 2 and created if necessary (splitting).
     * This tests assures that we do not remove parts of the way-network and we do
     * not have doubled ways.
     */
    @Test
    public void stage2WayNetworkCompletenessTest() {
        for (WayEntity way : testdata.stage1.ways.values()) {
            WayMappingComponent map = way.get(WayMappingComponent.class);
            assertNotNull(map);

            // find mappings
            HashSet<WaySliceMapping>              slices   = new HashSet<>(map.mapsto);
            ArrayList<ArrayList<WaySliceMapping>> mappings = new ArrayList<>();
            getAllMappingsRecursive(0, way.nodes.length - 1, slices, new LinkedList<>(), mappings);
            assertFalse(mappings.isEmpty());

            // the mapping must be unique on StreetGraph
            if (way.features.contains(testdata.streetgraph)) {
                assertEquals(1, mappings.size());    // has to be unique
                assertWayMappingValid(mappings.get(0), way);
                assertConnectorPropagation(testdata.stage1, testdata.stage2, mappings.get(0));

                // mapping may not be unique on non-StreetGraph ways
            } else {
                // make sure mappings are correct
                for (ArrayList<WaySliceMapping> mapping : mappings)
                    assertWayMappingValid(mapping, way);
            }
        }
    }

    /**
     * Check that every way in stage2 can be represented by a set of ways in stage
     * 1 (non-unique mapping). Also tests that the Connectors required for merging
     * are actually present, if a way has been merged in the unification process.
     * This test assures that we do not add non-existing parts to the way-network.
     * <p>
     * Note: We cannot test for uniqueness here, since the OpenStreetMap data-set
     * may contain double-geometry (this should not be the case, however the
     * data-set is user-data, so we cannot make any guarantees).
     */
    @Test
    public void stage2WayNetworkConsistencyTest() {
        for (WayEntity way : testdata.stage2.ways.values()) {
            WayMappingComponent map = way.get(WayMappingComponent.class);
            assertNotNull(map);

            // find mappings
            HashSet<WaySliceMapping>              slices   = new HashSet<>(map.mapsto);
            ArrayList<ArrayList<WaySliceMapping>> mappings = new ArrayList<>();
            getAllMappingsRecursive(0, way.nodes.length - 1, slices, new LinkedList<>(), mappings);

            assertFalse(mappings.isEmpty());

            // make sure mappings are correct
            for (ArrayList<WaySliceMapping> mapping : mappings)
                assertWayMappingValid(mapping, way);

            // make sure connector propagation is correct, if this is part of the StreetGraph
            if (way.features.contains(testdata.streetgraph)) {
                for (ArrayList<WaySliceMapping> mapping : mappings)
                    assertConnectorPropagation(testdata.stage2, testdata.stage1, mapping);
            }
        }
    }

    // TODO: tests for StreetGraph/StreetGraphGenerator
}

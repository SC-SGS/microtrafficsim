package microtrafficsim.osm.parser.processing.osm.sanitizer;

import microtrafficsim.core.map.features.info.LaneInfo;
import microtrafficsim.core.map.features.info.MaxspeedInfo;
import microtrafficsim.core.map.features.info.OnewayInfo;
import microtrafficsim.core.map.features.info.StreetType;
import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.processing.Processor;
import microtrafficsim.osm.parser.relations.RelationBase;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelation;
import microtrafficsim.osm.primitives.Primitive;
import microtrafficsim.utils.collections.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * A {@code Processor} for to sanitize the parsed {@code DataSet} before
 * actually processing it. This sanitizer requires the {@code
 * SanitizerWayComponent} to sucessfully sanitize the {@code StreetComponent}s.
 *
 * @author Maximilian Luz
 */
public class OSMDataSetSanitizer implements Processor {
    private static Logger logger = LoggerFactory.getLogger(OSMDataSetSanitizer.class);

    private OSMSanitizerValues values;


    /**
     * Creates a new {@code OSMDataSetSanitizer} using a new instance of {@code
     * DefaultOSMSanitizerValues} as {@code OSMSanitizerValues}.
     * <p>
     * This does the same as calling
     * {@link
     * OSMDataSetSanitizer#OSMDataSetSanitizer(OSMSanitizerValues)
     * OSMDataSetSanitizer(new DefaultOSMSanitizerValues())
     * }
     * </p>
     */
    public OSMDataSetSanitizer() {
        this(new DefaultOSMSanitizerValues());
    }

    /**
     * Creates a new {@code OSMDataSetSanitizer} using the given {@code
     * OSMSanitizerValues}.
     *
     * @param values the {@code OSMSanitizerValues} specifying the values to be
     *               used in the sanitizing-process (e.g. default-value).
     */
    public OSMDataSetSanitizer(OSMSanitizerValues values) {
        this.values = values;
    }


    @Override
    public void execute(Parser parser, DataSet dataset) {
        logger.info("executing sanitizer");

        sanitizeWays(dataset);
        sanitizeRestrictionRelations(dataset);

        // remove sanitizer components
        for (WayEntity way : dataset.ways.values()) {
            way.remove(SanitizerWayComponent.class);
        }

        logger.info("finished sanitizer:");
        logger.debug("\tNodes: " + dataset.nodes.size());
        logger.debug("\tWays:  " + dataset.ways.size());
        for (Class<? extends RelationBase> type : dataset.relations.getRelationTypes()) {
            logger.debug("\t" + type.getSimpleName() + ": " + dataset.relations.getAll(type).size());
        }
    }


    /**
     * Sanitizes all ways of the given {@code DataSet}, removes them if necessary.
     *
     * @param dataset the {@code DataSet} of which the {@code WayEntities} should be
     *                sanitized.
     */
    private void sanitizeWays(DataSet dataset) {
        for (WayEntity way : dataset.ways.values())
            sanitizeWay(way);

        ArrayList<Long> remove = new ArrayList<>();
        for (WayEntity way : dataset.ways.values())
            if (way.nodes.length < 2) remove.add(way.id);

        dataset.ways.keySet().removeAll(remove);
    }

    /**
     * Sanitizes the given {@code WayEntity}, including its {@code StreetComponent}
     * if available.
     *
     * @param way the {@code WayEntity} to sanitize.
     */
    private void sanitizeWay(WayEntity way) {
        // remove all sequential nodes with same id but one (i.e. [1,2,2,3] -> [1,2,3])
        if (way.nodes.length > 0) {
            long[] nodes = new long[way.nodes.length];
            nodes[0]     = way.nodes[0];

            int len = 1;
            for (int i = 1; i < way.nodes.length; i++) {
                if (nodes[len - 1] != way.nodes[i]) {
                    nodes[len] = way.nodes[i];
                    len++;
                }
            }

            if (len != way.nodes.length) way.nodes = Arrays.copyOf(nodes, len);
        }

        // sanitize components
        SanitizerWayComponent sancomp = way.get(SanitizerWayComponent.class);
        if (sancomp != null) { sanitizeStreetComponent(way.get(StreetComponent.class), sancomp); }
    }


    /**
     * Sanitize the given {@code StreetComponent} using the {@code SanitizerWayComponent}.
     *
     * @param streetcomp the {@code StreetComponent} to sanitize, if this is {@code
     *                   null} this method does nothing.
     * @param sancomp    the {@code SanitizerWayComponent} used for sanitizing.
     */
    private void sanitizeStreetComponent(StreetComponent streetcomp, SanitizerWayComponent sancomp) {
        if (streetcomp == null) return;

        if (streetcomp.streettype == null) streetcomp.streettype = StreetType.ROAD;

        sanitizeMaxspeedInfo(streetcomp.maxspeed, streetcomp.oneway, sancomp.highway);
        sanitizeLaneInfo(streetcomp.lanes, streetcomp.oneway, sancomp.highway);

        // XXX TEMPORARY
        if (streetcomp.lanes.forward > 0) streetcomp.lanes.forward = 1;

        if (streetcomp.lanes.backward > 0) streetcomp.lanes.backward = 1;

        streetcomp.lanes.sum = streetcomp.lanes.forward + streetcomp.lanes.backward;
    }

    /**
     * Sanitize the given {@code MaxspeedInfo}.
     *
     * @param maxspeed the {@code MaxspeedInfo} to sanitize.
     * @param highway  the highway-type of the {@code WayEntity} associated with the
     *                 given {@code MaxspeedInfo}.
     */
    private void sanitizeMaxspeedInfo(MaxspeedInfo maxspeed, OnewayInfo oneway, String highway) {
        if (oneway == OnewayInfo.NO || oneway == OnewayInfo.FORWARD || oneway == OnewayInfo.REVERSIBLE)
            maxspeed.forward = getSanitizedMaxspeedValue(maxspeed.forward, highway);
        else
            maxspeed.forward = 0;

        if (oneway == OnewayInfo.NO || oneway == OnewayInfo.BACKWARD)
            maxspeed.backward = getSanitizedMaxspeedValue(maxspeed.backward, highway);
        else
            maxspeed.backward = 0;
    }

    /**
     * Sanitizes the given max-speed value.
     *
     * @param value   the max-speed value to sanitize.
     * @param highway the highway-type of the {@code WayEntity} associated with the
     *                given max-speed value.
     * @return the sanitized max-speed value.
     */
    private float getSanitizedMaxspeedValue(float value, String highway) {
        if (value == MaxspeedInfo.NONE)
            return values.getMaximumSpeed();
        else if (value == MaxspeedInfo.SIGNALS)
            return values.getMaxspeedFromStreetType(highway);
        else if (value == MaxspeedInfo.UNGIVEN)
            return values.getMaxspeedFromStreetType(highway);
        else if (value == MaxspeedInfo.WALKING)
            return values.getWalkingSpeed();
        else
            return value;
    }

    /**
     * Sanitize the given {@code LaneInfo}.
     *
     * @param laneinfo the {@code LaneInfo} to sanitize.
     * @param oneway   the {@code OnewayInfo} associated with the given {@code
     *                 LaneInfo}.
     * @param highway  the highway-type of the {@code WayEntity} associated with the
     *                 given {@code LaneInfo}.
     */
    private void sanitizeLaneInfo(LaneInfo laneinfo, OnewayInfo oneway, String highway) {
        // TODO: cleanup

        int lanes    = laneinfo.sum;
        int forward  = laneinfo.forward;
        int backward = laneinfo.backward;

        if (oneway == OnewayInfo.NO) {
            // none given: use defaults
            if (lanes == LaneInfo.UNGIVEN && forward == LaneInfo.UNGIVEN && backward == LaneInfo.UNGIVEN) {
                forward = backward = values.getLanesPerDirectionFromHighwayType(highway);
                lanes              = forward + backward;

                // only backward given: set forward to default
            } else if (lanes == LaneInfo.UNGIVEN && forward == LaneInfo.UNGIVEN) {
                logger.warn("on tag 'lanes': 'oneway=no' but only 'lanes:backward' given, "
                            + "using 'lanes:forward=default'");
                forward = values.getLanesPerDirectionFromHighwayType(highway);
                lanes   = forward + backward;

                // only forward given: set backward to default
            } else if (lanes == LaneInfo.UNGIVEN && backward == LaneInfo.UNGIVEN) {
                logger.warn("on tag 'lanes': 'oneway=no' but only 'lanes:forward' given, "
                            + "using 'lanes:backward=default'");
                backward = values.getLanesPerDirectionFromHighwayType(highway);
                lanes    = forward + backward;

                // forward and backward given: deduce lanes
            } else if (lanes == LaneInfo.UNGIVEN) {
                lanes = forward + backward;

                // lanes given: deduce forward/backward
            } else if (forward == LaneInfo.UNGIVEN && backward == LaneInfo.UNGIVEN) {
                if (lanes == 1) {
                    forward = backward = 1;
                } else if (lanes % 2 == 0) {
                    forward = backward = lanes / 2;
                } else {
                    logger.warn("on tag 'lanes': 'lanes=*' is uneven but no 'lanes:forward' or "
                                + "'lanes:backward' given, adding one lane");
                    lanes++;
                    forward = backward = lanes / 2;
                }

                // lanes and backward given: deduce forward
            } else if (forward == LaneInfo.UNGIVEN) {
                forward = lanes - backward;
                if (forward < 1) {
                    logger.warn("on tag 'lanes': tried to deduce 'lanes:forward' from 'lanes'"
                                + "and 'lanes:backward' but result is smaller than 1 ('oneway=no'), "
                                + "using default value");
                    forward = values.getLanesPerDirectionFromHighwayType(highway);
                }

                // lanes and forward given: deduce backward
            } else if (backward == LaneInfo.UNGIVEN) {
                backward = lanes - forward;
                if (backward < 1) {
                    logger.warn("on tag 'lanes': tried to deduce 'lanes:backward' from 'lanes'"
                                + "and 'lanes:forward' but result is smaller than 1 ('oneway=no'), "
                                + "using default value");
                    backward = values.getLanesPerDirectionFromHighwayType(highway);
                }

                // everything present
            } else {
                if (lanes != 1 && lanes < forward + backward) {
                    logger.warn("on tag 'lanes': 'lanes' is smaller than 'lanes:forward' + "
                                + "'lanes:backward', adjusting 'lanes'");
                    lanes = forward + backward;
                }
            }

            if (forward == 0) {
                logger.warn("on tag 'lanes': 'lanes:forward=0' but 'oneway=no', assuming "
                            + "'lanes:forward=1");
                forward++;
                lanes = forward + backward;
            }

            if (backward == 0) {
                logger.warn("on tag 'lanes': 'lanes:backward=0' but 'oneway=no', assuming "
                            + "'lanes:backward=1'");
                backward++;
                lanes = forward + backward;
            }

        } else if (oneway == OnewayInfo.FORWARD) {
            if (backward > 0) {
                logger.warn("on tag 'lanes': 'lanes:backward=*' ignored when using with "
                            + "'oneway=yes'");

                if (forward != LaneInfo.UNGIVEN) lanes = forward;
            }
            backward = 0;

            // only forward given
            if (lanes == LaneInfo.UNGIVEN && forward != LaneInfo.UNGIVEN) {
                lanes = forward;

                // only lanes given
            } else if (lanes != LaneInfo.UNGIVEN && forward == LaneInfo.UNGIVEN) {
                forward = lanes;

                // none given: use default
            } else if (lanes == LaneInfo.UNGIVEN) {
                lanes = forward = values.getLanesPerDirectionFromHighwayType(highway);

            } else if (lanes != forward) {
                logger.warn("on tag 'lanes': 'lanes:forward' does not equal 'lanes' on "
                            + "way tagged with 'oneway=yes', using 'lanes:forward'");
                lanes = forward;
            }

        } else if (oneway == OnewayInfo.BACKWARD) {
            if (forward > 0) {
                logger.warn("on tag 'lanes': 'lanes:forward=*' ignored when using with "
                            + "'oneway=-1'");

                if (backward != LaneInfo.UNGIVEN) lanes = backward;
            }
            forward = 0;

            // only backward given
            if (lanes == LaneInfo.UNGIVEN && backward != LaneInfo.UNGIVEN) {
                lanes = backward;

                // only lanes given
            } else if (lanes != LaneInfo.UNGIVEN && backward == LaneInfo.UNGIVEN) {
                backward = lanes;

                // none given: use default
            } else if (lanes == LaneInfo.UNGIVEN) {
                lanes = backward = values.getLanesPerDirectionFromHighwayType(highway);

            } else if (lanes != backward) {
                logger.warn("on tag 'lanes': 'lanes:backward' does not equal 'lanes' on "
                            + "way tagged with 'oneway=-1', using 'lanes:backward'");
                lanes = backward;
            }

        } else if (oneway == OnewayInfo.REVERSIBLE) {
            // none given: use defaults
            if (lanes == LaneInfo.UNGIVEN && forward == LaneInfo.UNGIVEN && backward == LaneInfo.UNGIVEN) {
                lanes = forward = values.getLanesPerDirectionFromHighwayType(highway);
                backward        = 0;

                // backward given: use backward
            } else if (forward == LaneInfo.UNGIVEN && backward != LaneInfo.UNGIVEN) {
                lanes = forward = backward;
                backward        = 0;

                // forward given: use forward
            } else if (forward != LaneInfo.UNGIVEN && backward == LaneInfo.UNGIVEN) {
                lanes    = forward;
                backward = 0;

                // backward and forward given: just use forward
            } else if (lanes == LaneInfo.UNGIVEN) {
                lanes    = forward;
                backward = 0;

                // only lanes given: use lanes
            } else if (forward == LaneInfo.UNGIVEN) {
                forward  = lanes;
                backward = 0;

                // all given: use forward
            } else {
                lanes    = forward;
                backward = 0;
            }
        }

        laneinfo.sum      = forward + backward;
        laneinfo.forward  = forward;
        laneinfo.backward = backward;
    }


    /**
     * Sanitizes all {@code RestrictionRelations} of the given {@code DataSet},
     * removes them if necessary.
     *
     * @param dataset the {@code DataSet} of which the {@code RestrictionRelations}
     *                should be sanitized.
     */
    private void sanitizeRestrictionRelations(DataSet dataset) {
        ArrayList<Long> remove = new ArrayList<>();

        // remove all invalid restriction-relations
        for (RestrictionRelation r : dataset.relations.getAll(RestrictionRelation.class).values()) {

            // viaType must either be NODE or WAY
            if (r.viaType != Primitive.Type.NODE && r.viaType != Primitive.Type.WAY) {
                remove.add(r.id);
                continue;
            }

            // via must contain at least one member
            if (r.via.size() < 1) {
                remove.add(r.id);
                continue;
            }

            // only one via-node allowed
            if (r.viaType == Primitive.Type.NODE && r.via.size() > 1) {
                remove.add(r.id);
                continue;
            }

            // from must contain at least one member
            if (r.from.size() < 1) {
                remove.add(r.id);
                continue;
            }

            // to must contain at least one member
            if (r.to.size() < 1) {
                remove.add(r.id);
                continue;
            }

            // all referenced entities must be in the DataSet
            if (r.viaType == Primitive.Type.NODE && !dataset.nodes.containsKey(r.via.iterator().next())) {
                remove.add(r.id);
                continue;
            } else if (r.viaType == Primitive.Type.WAY && !dataset.ways.keySet().containsAll(r.via)) {
                remove.add(r.id);
                continue;
            }

            if (!dataset.ways.keySet().containsAll(r.from)) {
                remove.add(r.id);
                continue;
            }

            if (!dataset.ways.keySet().containsAll(r.to)) {
                remove.add(r.id);
                continue;
            }

            // 'from' and 'to' must be adjacent to 'via'
            if (r.viaType == Primitive.Type.NODE) {
                Long via = r.via.get(0);
                if (!(allWaysAdjacentToNode(dataset, r.from, via) && allWaysAdjacentToNode(dataset, r.to, via)))
                    remove.add(r.id);

            } else if (r.viaType == Primitive.Type.WAY) {
                if (!(allWaysAdjacentToSome(dataset, r.from, r.via) && allWaysAdjacentToSome(dataset, r.to, r.via)))
                    remove.add(r.id);
            }
        }

        dataset.relations.getAll(RestrictionRelation.class).keySet().removeAll(remove);
    }


    /**
     * Checks if all WayEntities given by {@code ways} are adjacent to the
     * NodeEntity {@code node}.
     * <p>
     * A {@code WayEntity} and {@code NodeEntity} are considered adjacent if the
     * {@code WayEntity} contains the {@code NodeEntity}.
     * </p>
     *
     * @param dataset the {@code DataSet} to which {@code ways} and {@code node}
     *                belong.
     * @param ways    a {@code Collection} of IDs to {@code WayEnties} which should
     *                be checked for adjacency against {@code node}.
     * @param node    the {@code NodeEntity} against which {@code ways} should be
     *                checked for adjacency.
     * @return {@code true} iff all {@code ways} are adjacent to {@code node}.
     */
    private boolean allWaysAdjacentToNode(DataSet dataset, Collection<Long> ways, long node) {
        for (Long way : ways)
            if (!ArrayUtils.contains(dataset.ways.get(way).nodes, node))
                return false;

        return true;
    }

    /**
     * Checks if the WayEntity given by {@code way} is adjacent to at least one
     * WayEntity contained in {@code ways}.
     * <p>
     * Adjacent is defined here in a loose way, meaning: Two WayEntities are
     * considered adjacent iff there is at least one NodeEntity shared by both.
     * </p>
     *
     * @param dataset the {@code DataSet} to which the {@code WayEntities} belong.
     * @param way     the ID of the {@code WayEntity} which is testet for adjacency to
     *                {@code some}.
     * @param ways    a {@code Collection} of IDs of {@code WayEntities} to which
     *                {@code way} is tested for adjacency.
     * @return {@code true} if {@code way} is adjacent to at least one way in
     * {@code ways}.
     */
    private boolean wayAdjacentToSome(DataSet dataset, long way, Collection<Long> ways) {
        for (Long to : ways)
            if (!ArrayUtils.disjoint(dataset.ways.get(way).nodes, dataset.ways.get(to).nodes))
                return true;

        return false;
    }

    /**
     * Checks if all WayEntities given by {@code adjacent} are adjacent to at least
     * one {@code WayEntity} given by {@code some}.
     * <p>
     * Adjacent is defined here in a loose way, meaning: Two WayEntities are
     * considered adjacent iff there is at least one NodeEntity shared by both.
     * </p>
     *
     * @param dataset  the {@code DataSet} to which the {@code WayEntities} belong.
     * @param adjacent the IDs of {@code WayEntities} which are all checked for
     *                 adjacency to at least one way in {@code some}.
     * @param some     the IDs of {@code WayEntities} against which {@code adjacent}
     *                 are checked for adjacency.
     * @return {@code true} if every way in {@code adjacent} is adjacent to at
     * least one way in {@code some}
     */
    private boolean allWaysAdjacentToSome(DataSet dataset, Collection<Long> adjacent, Collection<Long> some) {
        for (long way : adjacent)
            if (!wayAdjacentToSome(dataset, way, some))
                return false;

        return true;
    }
}

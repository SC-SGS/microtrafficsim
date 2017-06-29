package microtrafficsim.core.vis.utils;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.features.Street;


public class LaneOffset {
    public static double getCenterOffset(double lanewidth, Street street, boolean drivingOnTheRight) {
        double fwd = street.numLanesFwd;
        double bwd = street.numLanesBwd;

        if (drivingOnTheRight) {
            return lanewidth * (bwd - fwd) / 2.0;
        } else {
            return lanewidth * (fwd - bwd) / 2.0;
        }
    }

    public static double getLaneOffset(double lanewidth, DirectedEdge edge, int lane, boolean drivingOnTheRight) {
        Street street = edge.getEntity().getGeometry();

        double fwd = street.numLanesBwd;
        double bwd = street.numLanesBwd;

        double center = (bwd - fwd) / 2.0;      // offset according to direction of edge

        if (drivingOnTheRight) {
            return lanewidth * (center + (edge.getNumberOfLanes() - lane - 1) + 0.5);
        } else {
            return lanewidth * (center - (edge.getNumberOfLanes() - lane - 1) - 0.5);
        }
    }
}

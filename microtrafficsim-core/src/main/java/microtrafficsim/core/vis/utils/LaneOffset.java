package microtrafficsim.core.vis.utils;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.features.Street;


public class LaneOffset {

    /**
     * Returns the offset to the center (as in geometric center, not lane-wise) of the given street.
     *
     * @param lanewidth         the lane-width of one lane.
     * @param street            the street to get the offset for.
     * @param drivingOnTheRight whether the driving-priority is to the right or not.
     * @return the (signed) offset ot the center of the street.
     */
    public static double getCenterOffset(double lanewidth, Street street, boolean drivingOnTheRight) {
        double fwd = street.numLanesFwd;
        double bwd = street.numLanesBwd;

        if (drivingOnTheRight) {
            return lanewidth * (bwd - fwd) / 2.0;
        } else {
            return lanewidth * (fwd - bwd) / 2.0;
        }
    }

    /**
     * Returns the offset to the center line of the street.
     *
     * @param lanewidth         the lane-width of one lane.
     * @param street            the street to which the lane belongs.
     * @param drivingOnTheRight whether the driving-priority is to the right or not.
     * @return the (signed) offset to the center-line of the street. Only valid for two-way streets.
     */
    public static double getOffsetToCenterLine(double lanewidth, Street street, boolean drivingOnTheRight) {
        return 0.0;
    }

    /**
     * Returns the offset to the outer edge of the lane.
     *
     * @param lanewidth         the lane-width of one lane.
     * @param street            the street to which the lane belongs.
     * @param lane              the index of the lane.
     * @param forward           indicates the direction of the lane (i.e. true if the lane is forward)
     * @param drivingOnTheRight whether the driving-priority is to the right or not.
     * @return the (signed) offset from the center of the street to the outer edge of the specified lane.
     */
    public static double getOffsetToLaneEdge(double lanewidth, Street street, int lane, boolean forward,
                                             boolean drivingOnTheRight)
    {
        int numLanes = forward ? street.numLanesFwd : street.numLanesBwd;

        if (drivingOnTheRight == forward) {
            return lanewidth * (lane - numLanes);
        } else {
            return lanewidth * (numLanes - lane);
        }
    }

    /**
     * Returns the offset to the center of the specified lane.
     *
     * @param lanewidth         the lane-width of one lane.
     * @param edge              the edge on which the lane is.
     * @param lane              the index of the lane.
     * @param drivingOnTheRight whether the driving-priority is to the right or not.
     * @return the (signed) offset to the center of the specified lane, taking the direction of the edge into account.
     */
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

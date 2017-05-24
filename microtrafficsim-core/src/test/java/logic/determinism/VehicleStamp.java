package logic.determinism;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

/**
 * @author Dominic Parga Cacheiro
 */
public class VehicleStamp {
    public long id;
    public DirectedEdge.Lane lane; // todo lane
    public int cellPosition;

    public VehicleStamp(long id, DirectedEdge.Lane lane, int cellPosition) {
        this.id = id;
        this.lane = lane;
        this.cellPosition = cellPosition;
    }

    @Override
    public String toString() {
        LevelStringBuilder builder = new LevelStringBuilder()
                .setDefaultLevelSeparator()
                .setDefaultLevelSubString();

        builder.appendln("<" + getClass().getSimpleName() + ">").incLevel(); {
            builder.appendln("id = " + id);
            builder.appendln("cell position = " + cellPosition);
            builder.appendln(lane);
        } builder.decLevel().append("</" + getClass().getSimpleName() + ">");

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;

        if (!(obj instanceof VehicleStamp))
            return false;


        VehicleStamp stamp = (VehicleStamp) obj;

        if (id != stamp.id)
            return false;

        if (lane == null && stamp.lane != null)
            return false;
        if (lane != null) {
            if (stamp.lane == null)
                return false;

            if (lane.hashCode() != stamp.lane.hashCode())
                return false;
        }

        if (cellPosition != stamp.cellPosition)
            return false;

        return true;
    }
}
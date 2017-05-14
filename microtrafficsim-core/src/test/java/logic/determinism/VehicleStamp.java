package logic.determinism;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Dominic Parga Cacheiro
 */
public class VehicleStamp {
    public long id;
    public DirectedEdge edge; // todo lane
    public int cellPosition;

    public VehicleStamp(long id, DirectedEdge edge, int cellPosition) {
        this.id = id;
        this.edge = edge;
        this.cellPosition = cellPosition;
    }

    @Override
    public String toString() {
        LevelStringBuilder builder = new LevelStringBuilder()
                .setDefaultLevelSeparator()
                .setDefaultLevelSubString();

        builder.appendln("<" + VehicleStamp.class.getSimpleName() + ">").incLevel(); {
            builder.appendln("id = " + id);
            builder.appendln("cell position = " + cellPosition);
            builder.appendln(edge);
        } builder.decLevel().append("<\\" + VehicleStamp.class.getSimpleName() + ">");

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

        if (edge == null && stamp.edge != null)
            return false;
        if (edge != null) {
            if (stamp.edge == null)
                return false;

            if (edge.hashCode() != stamp.edge.hashCode())
                return false;
        }

        if (cellPosition != stamp.cellPosition)
            return false;

        return true;
    }
}
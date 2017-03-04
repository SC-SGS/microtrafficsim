package logic.determinism;

import microtrafficsim.core.logic.streets.DirectedEdge;

/**
 * @author Dominic Parga Cacheiro
 */
public class VehicleStamp {

    public DirectedEdge edge; // todo lane
    public int cellPosition;

    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;

        if (!(obj instanceof VehicleStamp))
            return false;

        VehicleStamp stamp = (VehicleStamp) obj;
        return edge == stamp.edge && cellPosition == stamp.cellPosition;
    }
}
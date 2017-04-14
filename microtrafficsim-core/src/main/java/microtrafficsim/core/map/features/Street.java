package microtrafficsim.core.map.features;

import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.map.Coordinate;


/**
 * Feature-primitive containing all data necessary for basic street rendering,
 * extending the {@code MultiLine} class.
 *
 * @author Maximilian Luz
 */
public class Street extends MultiLine {
    public final double   layer;
    public final double   length;
    public final double[] distances;
    private StreetEntity  entity;

    /**
     * Constructs a new {@code Street}.
     *
     * @param id        the (unique) id of the street.
     * @param nodes     the ordered set of coordinates describing the geometry of the street.
     * @param layer     the grid grid on which the street is.
     * @param length    the length of the street.
     * @param distances the distances of the single line segments.
     */
    public Street(long id, Coordinate[] nodes, double layer, double length, double[] distances) {
        super(id, nodes);

        this.entity    = null;
        this.layer     = layer;
        this.length    = length;
        this.distances = distances;
    }

    /**
     * Returns the {@code StreetEntity} of this street.
     *
     * @return the {@code StreetEntity} of this street.
     */
    public StreetEntity getEntity() {
        return entity;
    }

    /**
     * Sets the {@code StreetEntity} of this street.
     *
     * @param entity the new entity of this street.
     */
    public void setEntity(StreetEntity entity) {
        this.entity = entity;
    }
}

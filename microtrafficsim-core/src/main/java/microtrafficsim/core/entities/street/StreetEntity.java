package microtrafficsim.core.entities.street;

import microtrafficsim.core.map.features.Street;

/**
 * This street entity contains the logic and geometric parts of a street. This class is used for communication between
 * logic and visualization of a street.
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public class StreetEntity {
    private final LogicStreetEntity forward;
    private final LogicStreetEntity backward;
    private final Street geometry;

    /**
     * Default constructor.
     *
     * @param forward Forward part of the street entity
     * @param backward Backward part of the street entity
     * @param geometry This represents the
     */
    public StreetEntity(LogicStreetEntity forward, LogicStreetEntity backward, Street geometry) {
        this.forward  = forward;
        this.backward = backward;
        this.geometry = geometry;
    }


    /**
     * Returns the forward edge of this entity.
     *
     * @return the forward edge of this entity.
     */
    public LogicStreetEntity getForwardEdge() {
        return forward;
    }

    /**
     * Returns the backward edge of this entity.
     *
     * @return the backward edge of this entity.
     */
    public LogicStreetEntity getBackwardEdge() {
        return backward;
    }

    /**
     * Returns the geometry of this entity.
     *
     * @return the geometry of this entity.
     */
    public Street getGeometry() {
        return geometry;
    }
}
package microtrafficsim.core.entities.street;

/**
 * This interface represents the logic part of a street entity serving all methods used by other parts of this street
 * entity.
 *
 * @author Dominic Parga Cacheiro
 */
public interface LogicStreetEntity {

    /**
     * Returns the {@code StreetEntity} of this component.
     *
     * @return the {@code StreetEntity} of this component.
     */
    StreetEntity getEntity();

    /**
     * Sets the {@code StreetEntity} of this component.
     *
     * @param entity the new {@code StreetEntity} of this component.
     */
    void setEntity(StreetEntity entity);
}
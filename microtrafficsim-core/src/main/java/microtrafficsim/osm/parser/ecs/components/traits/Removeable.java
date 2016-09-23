package microtrafficsim.osm.parser.ecs.components.traits;

import microtrafficsim.osm.parser.base.DataSet;


/**
 * Interface to provide code for {@code Components} to be executed on the removal of an entity from the system.
 *
 * @author Maximilian Luz
 */
public interface Removeable {

    /**
     * This method will be executed prior to removal of the underlying entity from the system.
     *
     * @param dataset the {@code DataSet} to which the underlying entity belongs.
     */
    void remove(DataSet dataset);
}

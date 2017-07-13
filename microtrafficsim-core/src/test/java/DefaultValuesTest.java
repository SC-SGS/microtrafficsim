import microtrafficsim.examples.simulation.MeasurementExample;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Dominic Parga Cacheiro
 */
public class DefaultValuesTest {
    @Test
    public void areDefaultValuesNull() {
        assertEquals("Path to measurement results is set.",
                "simdata.csv",
                MeasurementExample.DEFAULT_FILE_OUT.getPath());
    }
}

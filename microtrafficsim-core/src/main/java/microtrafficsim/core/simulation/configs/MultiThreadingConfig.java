package microtrafficsim.core.simulation.configs;


/**
 * This class contains configurations for working parallel.
 *
 * @author Dominic Parga Cacheiro
 */
public final class MultiThreadingConfig {
    public int nThreads;
    public int vehiclesPerRunnable;
    public int nodesPerThread;

    /**
     * Just calls {@link #setup()}.
     */
    public MultiThreadingConfig() {
        setup();
    }

    /**
     * Setup the parameters of this config file.
     */
    public void setup() {
        nThreads            = 8;
        vehiclesPerRunnable = 300;
        nodesPerThread      = 500;
    }

    /**
     * Updates the parameter of this config file.
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(MultiThreadingConfig config) {
        nThreads            = config.nThreads;
        vehiclesPerRunnable = config.vehiclesPerRunnable;
        nodesPerThread      = config.nodesPerThread;
    }
}
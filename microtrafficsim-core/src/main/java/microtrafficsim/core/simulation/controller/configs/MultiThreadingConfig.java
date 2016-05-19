package microtrafficsim.core.simulation.controller.configs;

import microtrafficsim.utils.valuewrapper.ConcurrentValue;
import microtrafficsim.utils.valuewrapper.LazyFinalValue;

/**
 * This class contains configurations for working parallel. 
 *  
 * @author Dominic Parga Cacheiro
 */
public final class MultiThreadingConfig {
	
	private LazyFinalValue<Integer> nThreads;
    private ConcurrentValue<Integer> vehiclesPerRunnable;
    private ConcurrentValue<Integer> nodesPerThread;
	
	public MultiThreadingConfig() {
        reset();
	}

    void reset() {

		nThreads = new LazyFinalValue<>(8);
		vehiclesPerRunnable = new ConcurrentValue<>(200);
		nodesPerThread = new ConcurrentValue<>(500);
    }

    void reset(MultiThreadingConfig config) {
        nThreads = config.nThreads;
        vehiclesPerRunnable = config.vehiclesPerRunnable;
        nodesPerThread = config.nodesPerThread;
    }

    public LazyFinalValue<Integer> nThreads() { return nThreads; }
    public ConcurrentValue<Integer> vehiclesPerRunnable() { return vehiclesPerRunnable; }
    public ConcurrentValue<Integer> nodesPerThread() { return nodesPerThread; }
}
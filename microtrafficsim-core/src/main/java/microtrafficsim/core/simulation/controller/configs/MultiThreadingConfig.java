package microtrafficsim.core.simulation.controller.configs;

/**
 * This class contains configurations for working parallel. 
 *  
 * @author Dominic Parga Cacheiro
 */
public class MultiThreadingConfig {
	
	public int nThreads;
	public int vehiclesPerRunnable;
	public int nodesPerThread;
	
	{
		nThreads = 8;
		vehiclesPerRunnable = 200;
		nodesPerThread = 500;
	}
}
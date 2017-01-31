package microtrafficsim.core.simulation.core;

/**
 *
 *
 * @author Dominic Parga Cacheiro
 */
public interface StepListener {

    /**
     * @param simulation this simulation has finished its step
     */
    void didOneStep(Simulation simulation);
}

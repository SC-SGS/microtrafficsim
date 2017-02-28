package microtrafficsim.core.logic.vehicles.driver;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Basic implementation of {@code Driver}.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicDriver implements Driver {

    /* general */
    private final ReentrantLock lock_priorityCounter;
    private final Random        random;

    /* variable information */
    private Route<Node> route;

    /* dynamic information */
    private int travellingTime;
    private int priorityCounter;    // for crossing logic
    /* Hulk */
    private final int maxAnger;
    private       int anger;
    private       int totalAnger;

    /* fix information */
    private       Vehicle vehicle;
    private final float   dawdleFactor;

    /**
     * Calls {@link #BasicDriver(long, int) BasicDriver(seed, 0)}
     */
    public BasicDriver(long seed) {
        this(seed, 0);
    }

    /**
     * Calls {@link #BasicDriver(long, float, int) BasicDriver(seed, 0.2f, spawndelay)}
     */
    public BasicDriver(long seed, int spawndelay) {
        this(seed, 0.2f, spawndelay);
    }

    /**
     * Calls {@link #BasicDriver(long, float, int) BasicDriver(seed, dawdleFactor, 0)}
     */
    public BasicDriver(long seed, float dawdleFactor) {
        this(seed, dawdleFactor, 0);
    }

    /**
     * @param seed         seed for {@link Random}, e.g. used for dawdling
     * @param dawdleFactor probability to dawdle (after Nagel-Schreckenberg-model)
     * @param spawndelay   after this number of simulation steps, this driver starts travelling
     */
    public BasicDriver(long seed, float dawdleFactor, int spawndelay) {

        /* general */
        lock_priorityCounter = new ReentrantLock(true);
        random               = new Random(seed);

        /* variable information */
        route = null;

        /* dynamic information */
        this.travellingTime = -spawndelay;
        resetPriorityCounter();
        maxAnger   = Integer.MAX_VALUE;
        anger      = 0;
        totalAnger = 0;

        /* fix information */
        vehicle         = null;
        String errorMsg = null;
        if (dawdleFactor > 1) {
            this.dawdleFactor = 1;
            errorMsg = "It must hold: 0 <= dawdleFactor <= 1\nCurrent: " + dawdleFactor + "\nValue set to 1.";
        } else if (dawdleFactor < 0) {
            this.dawdleFactor = 0;
            errorMsg = "It must hold: 0 <= dawdleFactor <= 1\nCurrent: " + dawdleFactor + "\nValue set to 0.";
        } else
            this.dawdleFactor = dawdleFactor;

        if (errorMsg != null)
            try {
                throw new Exception(errorMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public String toString() {
        LevelStringBuilder strBuilder = new LevelStringBuilder();
        strBuilder.appendln("<driver>");
        strBuilder.incLevel();

        strBuilder.appendln("seed  = " + random.getSeed());
        strBuilder.appendln("priority counter = " + priorityCounter);
        strBuilder.append(route);

        strBuilder.decLevel();
        strBuilder.appendln("<\\driver>");
        return strBuilder.toString();
    }

    /*
    |============|
    | (i) Driver |
    |============|
    */
    @Override
    public int accelerate(int tmpV) {
        return tmpV + 1;
    }

    @Override
    public int dawdle(int tmpV) {
        if (tmpV < 1)
            return 0;
        // Dawdling only 5km/h => return tmpV - 5
        if (random.nextFloat() < dawdleFactor)
            return tmpV - 1;
        return tmpV;
    }

    @Override
    public int getMaxVelocity() {
        DirectedEdge edge = vehicle.getDirectedEdge();
        if (edge == null)
            return Integer.MAX_VALUE;
        else
            return edge.getMaxVelocity();
    }

    @Override
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public Route<Node> getRoute() {
        return route;
    }

    @Override
    public void setRoute(Route<Node> route) {
        this.route = route;
    }

    @Override
    public int getTravellingTime() {
        return travellingTime;
    }

    @Override
    public void incTravellingTime() {
        travellingTime++;
    }

    @Override
    public int getPriorityCounter() {
        lock_priorityCounter.lock();
        int tmp = priorityCounter;
        lock_priorityCounter.unlock();

        return tmp;
    }

    @Override
    public void resetPriorityCounter() {
        lock_priorityCounter.lock();

        priorityCounter = 0;

        lock_priorityCounter.unlock();
    }

    @Override
    public void incPriorityCounter() {
        lock_priorityCounter.lock();

        int old = priorityCounter;
        priorityCounter++;
        if (old > priorityCounter) {
            try {
                throw new Exception(getClass().getSimpleName() + ".incPriorityCounter() - int overflow");
            } catch (Exception e) { e.printStackTrace(); }
        }

        lock_priorityCounter.unlock();
    }

    @Override
    public void decPriorityCounter() {
        lock_priorityCounter.lock();

        int old = priorityCounter;
        priorityCounter--;
        if (old < priorityCounter) {
            try {
                throw new Exception(getClass().getSimpleName() + ".incPriorityCounter() - int underflow");
            } catch (Exception e) { e.printStackTrace(); }
        }

        lock_priorityCounter.unlock();
    }

    /*
    |==========|
    | (i) Hulk |
    |==========|
    */
    /**
     * Increases the anger by 1 up to a maximum of {@link #getMaxAnger()}
     */
    @Override
    public void becomeMoreAngry() {
        if (anger < maxAnger)
            anger++;
        totalAnger += 1;
    }

    /**
     * Decreases the anger by 1 down to a minimum of 0.
     */
    @Override
    public void calmDown() {
        if (anger > 0)
            anger--;
    }

    @Override
    public int getAnger() {
        return anger;
    }

    @Override
    public int getTotalAnger() {
        return totalAnger;
    }

    @Override
    public int getMaxAnger() {
        return maxAnger;
    }
}

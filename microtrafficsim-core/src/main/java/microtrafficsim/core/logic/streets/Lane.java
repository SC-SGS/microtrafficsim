package microtrafficsim.core.logic.streets;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class represents one street lane inside a {@link DirectedEdge}.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Lane implements Resettable {
    public final ReentrantLock lock;
    private DirectedEdge associatedEdge;
    private int index;    // index in the list of lanes from the associated edge
    private HashMap<Integer, Vehicle> cells;
//    private Vehicle lastVehicle;
    private Vehicle lastVehicle;

    Lane(DirectedEdge container, int index) {
        lock                = new ReentrantLock(true);
        this.associatedEdge = container;
        this.index          = index;
        cells               = new HashMap<>();
        lastVehicle         = null;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(associatedEdge).add(index).getHash();
    }

    /**
     * Clears all cells and sets the last vehicle to null.
     */
    @Override
    public void reset() {
        this.cells.clear();
        this.lastVehicle = null;
    }

    public synchronized int getVehicleCount() {
        return cells.size();
    }

    public DirectedEdge getAssociatedEdge() {
        return associatedEdge;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        LevelStringBuilder stringBuilder = new LevelStringBuilder();
        stringBuilder.appendln("<Lane>").incLevel(); {
            stringBuilder.appendln("edge hash  = " + associatedEdge.hashCode());
            stringBuilder.appendln("lane index = " + index);
        } stringBuilder.decLevel().appendln("<\\Lane>");

        return stringBuilder.toString();
    }

    /*
    |=======================|
    | vehicle communication |
    |=======================|
    */
    public synchronized int getMaxInsertionIndex() {
        if (lastVehicle == null) {
            return associatedEdge.getLength() - 1;
        } else {
            return lastVehicle.getCellPosition() - 1;
        }
    }

    public synchronized void moveVehicle(Vehicle vehicle, int delta) {
        if (delta != 0) {
            checkedRemovingVehicle(vehicle);
            putNewVehicle(vehicle, vehicle.getCellPosition() + delta);
        }
    }

    public synchronized void insertVehicle(Vehicle vehicle, int pos) {
        if (putNewVehicle(vehicle, pos)) {
            /* update last vehicle */
            Vehicle newLastVehicle = vehicle;
            if (lastVehicle != null)
                if (lastVehicle.getCellPosition() < pos)
                    newLastVehicle = lastVehicle;
            lastVehicle = newLastVehicle;
        }
    }

    public synchronized void removeVehicle(Vehicle vehicle) {
        checkedRemovingVehicle(vehicle);
        if (cells.size() == 0) lastVehicle = null;
    }

    public synchronized Vehicle getLastVehicle() {
        return lastVehicle;
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    /**
     * @return true if success (<=> no vehicle has been on the given position)
     */
    private boolean checkedRemovingVehicle(Vehicle vehicle) {
        Vehicle removedVehicle = cells.remove(vehicle.getCellPosition());
        boolean success = removedVehicle == vehicle;

        BasicStringBuilder builder = new BasicStringBuilder();
        builder.appendln("Removing a vehicle from the lane removed an unexpected, different vehicle.")
                .appendln("EXPECTED = " + vehicle)
                .appendln("ACTUALLY REMOVED  = " + removedVehicle);
        assert success : builder.toString();

        return success;
    }

    /**
     * @return true if success (<=> no vehicle has been on the given position)
     */
    private boolean putNewVehicle(Vehicle vehicle, int pos) {
        Vehicle removedVehicle = cells.put(pos, vehicle);
        boolean success = removedVehicle == null;

        BasicStringBuilder builder = new BasicStringBuilder();
        builder.appendln("Inserting a vehicle to the lane removed a vehicle.")
                .appendln("INSERTED = " + vehicle)
                .appendln("REMOVED = " + removedVehicle);
        assert success : builder.toString();

        return success;
    }
}

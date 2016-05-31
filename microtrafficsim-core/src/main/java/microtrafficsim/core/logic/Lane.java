package microtrafficsim.core.logic;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.HashMap;

/**
 * This class represents one street lane inside a {@link DirectedEdge}.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Lane {

	private DirectedEdge associatedEdge;
	private int index; // index in the list of lanes from the associated edge
	private HashMap<Integer, AbstractVehicle> cells;
	private AbstractVehicle lastVehicle;
	public final Object lock = new Object();
	
	Lane(DirectedEdge container, int index) {
		this.associatedEdge = container;
		this.index = index;
		cells = new HashMap<>();
		lastVehicle = null;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder().add(associatedEdge).add(index).getHash();
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
		return associatedEdge + "." + index;
	}

	// |=======================|
	// | vehicle communication |
	// |=======================|
	public synchronized int getMaxInsertionIndex() {
		if (lastVehicle == null)
			return associatedEdge.getLength() - 1;
		else
			return lastVehicle.getCellPosition() - 1;
	}

	public synchronized void moveVehicle(AbstractVehicle vehicle, int delta) {
        if (delta > 0)
		    cells.remove(vehicle.getCellPosition());
		    cells.put(vehicle.getCellPosition() + delta, vehicle);
	}

	public synchronized void insertVehicle(AbstractVehicle vehicle, int pos) {
		AbstractVehicle removedVehicle = cells.put(pos, vehicle);
		if (removedVehicle != null)
			try {
				throw new Exception(
						"Inserting (\n" + vehicle +"\n) to the lane removed vehicle (\n" + removedVehicle + "\n)");
			} catch (Exception e) {
				e.printStackTrace();
			}
		lastVehicle = vehicle;
	}

	public synchronized void removeVehicle(AbstractVehicle vehicle) {
		AbstractVehicle removedVehicle = cells.remove(vehicle.getCellPosition());
		if (removedVehicle != vehicle)
			try {
				throw new Exception(
						"Removed vehicle (" + removedVehicle + ") in lane is not the expected one (" + vehicle + "). ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (cells.size() == 0)
			lastVehicle = null;
	}
	
	public synchronized AbstractVehicle getLastVehicle() {
		return lastVehicle;
	}
}
package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.Node;
import microtrafficsim.utils.collections.Triple;

import java.util.Iterator;

/**
 * A basic wrapper implementation for {@code ODMatrix} unsupporting methods that changes the original data.
 *
 * @author Dominic Parga Cacheiro
 */
public class UnmodifiableODMatrix implements ODMatrix {

    private ODMatrix odMatrix;

    /**
     * Default constructor
     */
    public UnmodifiableODMatrix(ODMatrix odMatrix) {
        this.odMatrix = odMatrix;
    }

    /*
    |==============|
    | (i) ODMatrix |
    |==============|
    */
    /**
     * Unsupported
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void inc(Node origin, Node destination) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void dec(Node origin, Node destination) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void set(int count, Node origin, Node destination) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void add(int count, Node origin, Node destination) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int get(Node origin, Node destination) {
        return odMatrix.get(origin, destination);
    }

    /**
     * @return a new instance of {@code UnmodifiableODMatrix} wrapping the matrix of this
     */
    @Override
    public ODMatrix shallowcopy() {
        return new UnmodifiableODMatrix(odMatrix);
    }

    /**
     * Unsupported
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public SparseODMatrix add(ODMatrix odMatrix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Triple<Node, Node, Integer>> iterator() {
        return odMatrix.iterator();
    }

    /**
     * Unsupported
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}

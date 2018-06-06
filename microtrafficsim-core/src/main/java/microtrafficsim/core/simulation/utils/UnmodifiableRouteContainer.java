package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.math.random.distributions.impl.Random;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Dominic Parga Cacheiro
 */
public class UnmodifiableRouteContainer implements RouteContainer {
    private final RouteContainer container;


    public UnmodifiableRouteContainer(RouteContainer container) {
        this.container = container;
    }


    @Override
    public int size() {
        return container.size();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return container.contains(o);
    }

    @Override
    public Iterator<Route> iterator() {
        return container.iterator();
    }

    @Override
    public Object[] toArray() {
        return container.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return container.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return container.containsAll(c);
    }


    /*
    |=============|
    | unsupported |
    |=============|
    */
    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public Route getRdm(Random random) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void addAll(Scenario scenario) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean add(Route shortestPathEdges) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(Collection<? extends Route> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}

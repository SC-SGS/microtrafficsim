package microtrafficsim.utils.collections.skiplist;

import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.StringBuilder;

/**
 * @author Dominic Parga Cacheiro
 */
class Skipnode<T> {

    private T        value;
    private Tower<T> tower;


    public Skipnode() {
        this(null);
    }

    public Skipnode(T value) {
        this.value = value;
        tower      = new Tower<>();
    }


    @Override
    public String toString() {
        StringBuilder builder = new BasicStringBuilder();

        builder.append(tower + " || value = " + value);

        return builder.toString();
    }

    /**
     * This method could be needed for the default comparator in a skiplist using hashcode (e.g. {@link PrioritySkipList}).
     *
     * @return hashcode of the stored value
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }


    public int getTowerHeight() {
        return tower.getHeight();
    }

    public void addToTower(Skipnode<T> prev, Skipnode<T> next) {
        tower.add(prev, next);
    }

    public void clear() {
        value = null;
        tower.clear();
    }

    /**
     * Removes the highest link (next + prev) of this tower
     */
    public void removeHighest() {
        tower.removeHighest();
    }


    public Skipnode<T> getNext() {
        return tower.getNext(0);
    }

    public Skipnode<T> getNext(int towerLevel) {
        return tower.getNext(towerLevel);
    }

    public void setNext(Skipnode<T> next) {
        tower.setNext(0, next);
    }

    public void setNext(int towerLevel, Skipnode<T> next) {
        tower.setNext(towerLevel, next);
    }


    public Skipnode<T> getPrev() {
        return tower.getPrev(0);
    }

    public Skipnode<T> getPrev(int towerLevel) {
        return tower.getPrev(towerLevel);
    }

    public void setPrev(Skipnode<T> prev) {
        tower.setPrev(0, prev);
    }

    public void setPrev(int towerLevel, Skipnode<T> prev) {
        tower.setPrev(towerLevel, prev);
    }
}
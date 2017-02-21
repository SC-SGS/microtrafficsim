package microtrafficsim.utils.collections.skiplist;

import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.StringBuilder;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
class SkipNode<T> {

    private T                      value;
    private ArrayList<SkipNode<T>> tower;


    public SkipNode() {
        this(null);
    }

    public SkipNode(T value) {
        this.value = value;
        tower      = new ArrayList<>(2);
        addToTower(this, this);
    }


    @Override
    public String toString() {
        StringBuilder builder = new BasicStringBuilder();

        builder.append("tower.height = " + getTowerHeight());
        builder.append(" || value = " + value);

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


    public void addToTower(SkipNode<T> prev, SkipNode<T> next) {
        tower.add(next);
        tower.add(prev);
    }

    /**
     * Important note: the height of the tower describes the count of all links in this node without the node itself.
     * E.g. if all towers would have a height of 0, the skip list would be a doubled linked list.
     *
     * @return the size of the tower containing all links to other nodes.
     */
    public int getTowerHeight() {
        return tower.size() / 2 - 1;
    }


    public SkipNode<T> getNext() {
        return getNext(0);
    }

    public SkipNode<T> getNext(int towerLevel) {
        return tower.get(getTowerNextIdx(towerLevel));
    }

    public void setNext(SkipNode<T> next) {
        setNext(next, 0);
    }

    public void setNext(SkipNode<T> next, int towerLevel) {
        tower.set(getTowerNextIdx(towerLevel), next);
    }


//    public void setNextLinkLength()


    public SkipNode<T> getPrev() {
        return getPrev(0);
    }

    public SkipNode<T> getPrev(int towerLevel) {
        return tower.get(getTowerPrevIdx(towerLevel));
    }

    public void setPrev(SkipNode<T> prev) {
        setPrev(prev, 0);
    }

    public void setPrev(SkipNode<T> prev, int towerLevel) {
        tower.set(getTowerPrevIdx(towerLevel), prev);
    }


    public void clear() {
        value = null;
        tower.clear();
    }

    /**
     * Removes the highest link (next + prev) of this tower
     */
    public void removeHighest() {
        tower.remove(tower.size() - 1);
        tower.remove(tower.size() - 1);
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    private int getTowerNextIdx(int towerLevel) {
        return 2 * towerLevel;
    }

    private int getTowerPrevIdx(int towerLevel) {
        return 2 * towerLevel + 1;
    }
}
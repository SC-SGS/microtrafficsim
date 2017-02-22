package microtrafficsim.utils.collections.skiplist;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
class Tower<T> {

    private ArrayList<Skipnode<T>> skipnodes;
    private ArrayList<Integer>     linkLengths;

    public Tower() {
        // (1) for every element this tower has to store two links: next and prev
        // (2) due to geometric distribution, this tower has to store more than 1 element (2 links) in every 4th case
        skipnodes = new ArrayList<>(2);
        linkLengths = new ArrayList<>(2);
    }

    @Override
    public String toString() {
        return "tower.height = " + getHeight();
    }


    /**
     * Important note: the height of the tower describes the count of all links in this node without the node itself.
     * E.g. if all towers would have a height of 0, the skip list would be a doubled linked list.
     *
     * @return the size of the tower containing all links to other nodes.
     */
    public int getHeight() {
        return skipnodes.size() / 2 - 1;
    }


    public void add(Skipnode<T> prev, Skipnode<T> next) {
        // order is important due to idx; see getNextIdx(int towerLevel)
        skipnodes.add(next);
        skipnodes.add(prev);

        linkLengths.add(1);
        linkLengths.add(1);
    }

    /**
     * Removes the highest link (next + prev) of this tower
     */
    public void removeHighest() {
        skipnodes.remove(skipnodes.size() - 1);
        skipnodes.remove(skipnodes.size() - 1);

        linkLengths.remove(linkLengths.size() - 1);
        linkLengths.remove(linkLengths.size() - 1);
    }

    public void clear() {
        skipnodes.clear();
        linkLengths.clear();
    }


    public Skipnode<T> getNext(int towerLevel) {
        return skipnodes.get(getNextIdx(towerLevel));
    }

    public void setNext(int towerLevel, Skipnode<T> next) {
        skipnodes.set(getNextIdx(towerLevel), next);
    }

    public Skipnode<T> getPrev(int towerLevel) {
        return skipnodes.get(getPrevIdx(towerLevel));
    }

    public void setPrev(int towerLevel, Skipnode<T> prev) {
        skipnodes.set(getPrevIdx(towerLevel), prev);
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    private int getNextIdx(int towerLevel) {
        return 2 * towerLevel;
    }

    private int getPrevIdx(int towerLevel) {
        return 2 * towerLevel + 1;
    }
}

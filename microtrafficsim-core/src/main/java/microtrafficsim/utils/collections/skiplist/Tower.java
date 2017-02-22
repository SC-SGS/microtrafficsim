package microtrafficsim.utils.collections.skiplist;

import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
class Tower<E> {

    private ArrayList<Skipnode<E>> skipnodes;
    private ArrayList<Integer>     linkLengths;

    public Tower() {
        // (1) for every element this tower has to store two links: next and prev
        // (2) due to geometric distribution, this tower has to store more than 1 element (2 links) in every 4th case
        skipnodes = new ArrayList<>(2);
        // only (2) holds
        linkLengths = new ArrayList<>(1);
    }

    @Override
    public String toString() {
        LevelStringBuilder builder = new LevelStringBuilder();
        builder.appendln("<tower>");
        builder.incLevel();

        for (int towerLevel = getHeight(); towerLevel >= 0; towerLevel--) {
            builder.appendln("| |-" + getLinkLength(towerLevel) + "->");
        }

        builder.decLevel();
        builder.append("</tower>");
        return builder.toString();
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


    public void add(Skipnode<E> prev, Skipnode<E> next) {
        // order is important due to idx; see getNextIdx(int towerLevel)
        skipnodes.add(next);
        skipnodes.add(prev);
    }

    public void addLinkLength(int linkLength) {
        linkLengths.add(linkLength);
    }

    /**
     * Removes the highest link (next + prev) of this tower
     */
    public void removeHighest() {
        skipnodes.remove(skipnodes.size() - 1);
        skipnodes.remove(skipnodes.size() - 1);

        linkLengths.remove(linkLengths.size() - 1);
    }

    public void clear() {
        skipnodes.clear();
        linkLengths.clear();
    }


    public Skipnode<E> getNext() {
        return getNext(0);
    }

    public Skipnode<E> getNext(int towerLevel) {
        return skipnodes.get(getNextIdx(towerLevel));
    }

    public void setNext(Skipnode<E> next) {
        setNext(0, next);
    }

    public void setNext(int towerLevel, Skipnode<E> next) {
        skipnodes.set(getNextIdx(towerLevel), next);
    }

    public Skipnode<E> getPrev() {
        return getPrev(0);
    }

    public Skipnode<E> getPrev(int towerLevel) {
        return skipnodes.get(getPrevIdx(towerLevel));
    }

    public void setPrev(Skipnode<E> prev) {
        setPrev(0, prev);
    }

    public void setPrev(int towerLevel, Skipnode<E> prev) {
        skipnodes.set(getPrevIdx(towerLevel), prev);
    }


    public int getLinkLength(int towerLevel) {
        return linkLengths.get(towerLevel);
    }

    public void setLinkLength(int towerLevel, int linkLength) {
        linkLengths.set(towerLevel, linkLength);
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

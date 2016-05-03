package microtrafficsim.utils.datacollection;

/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public interface DataCollector<T> extends Iterable<T> {

    public boolean addBundle(String tag);
    public void put(String tag, T t);
    public int size(String tag);
}

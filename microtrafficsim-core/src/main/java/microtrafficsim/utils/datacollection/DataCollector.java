package microtrafficsim.utils.datacollection;


/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public interface DataCollector<T> extends Iterable<T> {
    boolean addBundle(String tag);
    void put(String tag, T t);
    int size(String tag);
}

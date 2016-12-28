package microtrafficsim.utils.collections;

/**
 * Java has no triple...
 *
 * @author Dominic Parga Cacheiro
 */
public class Triple<T, U, V> {

    public final T obj0;
    public final U obj1;
    public final V obj2;

    public Triple(T obj0, U obj1, V obj2) {
        this.obj0 = obj0;
        this.obj1 = obj1;
        this.obj2 = obj2;
    }
}

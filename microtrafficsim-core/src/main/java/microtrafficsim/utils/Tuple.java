package microtrafficsim.utils;

/**
 * Java has no tuples..
 *
 * @author Dominic Parga Cacheiro
 */
public class Tuple<U, V> {

    public final U obj0;
    public final V obj1;

    public Tuple(U obj0, V obj1) {
        this.obj0 = obj0;
        this.obj1 = obj1;
    }
}

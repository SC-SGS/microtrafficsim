package microtrafficsim.utils.functional;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Procedure2<T1, T2> {
    void invoke(T1 t1, T2 t2);
}

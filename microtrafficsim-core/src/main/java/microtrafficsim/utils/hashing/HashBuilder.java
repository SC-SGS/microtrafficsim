package microtrafficsim.utils.hashing;


import microtrafficsim.utils.Resettable;

/**
 * Interface for a simple hash-generator.
 *
 * @author Maximilian Luz
 */
public interface HashBuilder extends Resettable {

    /**
     * Returns the generated hash which is constructed by applying an
     * implementation-specific hash-algorithm to the added objects.
     *
     * @return the generated hash.
     */
    int getHash();

    /**
     * Resets this builders internal state.
     */
    @Override
    void reset();

    /**
     * Add an object as influence to the generated hash.
     *
     * @param obj the object to add.
     * @return {@code this}.
     */
    HashBuilder add(Object obj);

    /**
     * Add a byte as influence to the generated hash.
     *
     * @param obj the byte to add.
     * @return {@code this}.
     */
    default HashBuilder add(byte obj) {
        return this.add(Byte.valueOf(obj));
    }

    /**
     * Add a short as influence to the generated hash.
     *
     * @param obj the short to add.
     * @return {@code this}.
     */
    default HashBuilder add(short obj) {
        return this.add(Short.valueOf(obj));
    }

    /**
     * Add an integer as influence to the generated hash.
     *
     * @param obj the integer to add.
     * @return {@code this}.
     */
    default HashBuilder add(int obj) {
        return this.add(Integer.valueOf(obj));
    }

    /**
     * Add a long as influence to the generated hash.
     *
     * @param obj the long to add.
     * @return {@code this}.
     */
    default HashBuilder add(long obj) {
        return this.add(Long.valueOf(obj));
    }

    /**
     * Add a float as influence to the generated hash.
     *
     * @param obj the float to add.
     * @return {@code this}.
     */
    default HashBuilder add(float obj) {
        return this.add(Float.valueOf(obj));
    }

    /**
     * Add a double as influence to the generated hash.
     *
     * @param obj the double to add.
     * @return {@code this}.
     */
    default HashBuilder add(double obj) {
        return this.add(Double.valueOf(obj));
    }

    /**
     * Add a character as influence to the generated hash.
     *
     * @param obj the character to add.
     * @return {@code this}.
     */
    default HashBuilder add(char obj) {
        return this.add(Character.valueOf(obj));
    }
}

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
     * Add an array of objects as influence to the generated hash.
     *
     * @param obj the array of objects to add.
     * @return {@code this}.
     */
    default <T> HashBuilder add(T[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (T e : obj) {
                add(e);
            }
        }
        return this;
    }

    /**
     * Add an array of booleans as influence to the generated hash.
     *
     * @param obj the array of booleans to add.
     * @return {@code this}.
     */
    default HashBuilder add(boolean[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (boolean e : obj) {
                add(e);
            }
        }
        return this;
    }

    /**
     * Add an array of bytes as influence to the generated hash.
     *
     * @param obj the array of bytes to add.
     * @return {@code this}.
     */
    default HashBuilder add(byte[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (byte e : obj) {
                add(e);
            }
        }
        return this;
    }

    /**
     * Add an array of shorts as influence to the generated hash.
     *
     * @param obj the array of shorts to add.
     * @return {@code this}.
     */
    default HashBuilder add(short[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (short e : obj) {
                add(e);
            }
        }
        return this;
    }

    /**
     * Add an array of ints as influence to the generated hash.
     *
     * @param obj the array of ints to add.
     * @return {@code this}.
     */
    default HashBuilder add(int[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (int e : obj) {
                add(e);
            }
        }
        return this;
    }

    /**
     * Add an array of longs as influence to the generated hash.
     *
     * @param obj the array of longs to add.
     * @return {@code this}.
     */
    default HashBuilder add(long[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (long e : obj) {
                add(e);
            }
        }
        return this;
    }

    /**
     * Add an array of floats as influence to the generated hash.
     *
     * @param obj the array of floats to add.
     * @return {@code this}.
     */
    default HashBuilder add(float[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (float e : obj) {
                add(e);
            }
        }
        return this;
    }

    /**
     * Add an array of doubles as influence to the generated hash.
     *
     * @param obj the array of doubles to add.
     * @return {@code this}.
     */
    default HashBuilder add(double[] obj) {
        if (obj == null) {
            add((Object) null);
        } else {
            for (double e : obj) {
                add(e);
            }
        }

        return this;
    }

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

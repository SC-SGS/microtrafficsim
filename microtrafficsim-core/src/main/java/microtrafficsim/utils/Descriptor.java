package microtrafficsim.utils;

import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.Objects;

/**
 * <p>
 * This is a wrapper class for any object type adding a string description to it.
 *
 * <p>
 * Per default, this class assumes to be an object of the type {@code T}, so {@link Object} methods are referring to
 * the wrapped object. Exception is {@link Object#toString()} using the {@code String} description as well.
 *
 * @author Dominic Parga Cacheiro
 */
public class Descriptor<T> {

    private final String description;
    private final T obj;

    public Descriptor(T obj, String description) {
        this.obj = obj;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public T getObj() {
        return obj;
    }


    @Override
    public int hashCode() {
        return obj.hashCode();
    }

    @Override
    public boolean equals(Object obj) {


        if (obj == this)
            return true;

        if (!(obj instanceof Descriptor<?>))
            return obj.equals(this.obj);

        Descriptor<?> other = (Descriptor<?>) obj;
        return this.obj.equals(other.obj);
    }

    @Override
    public String toString() {
        LevelStringBuilder builder = new LevelStringBuilder();
        builder.setLevelSeparator(System.lineSeparator());
        builder.setLevelSubString("    ");


        builder.appendln("<Descriptor>").incLevel();

        builder.appendln("<description>").incLevel();
        builder.appendln(description);
        builder.decLevel().appendln("</description>");

        builder.appendln("<object>").incLevel();
        builder.appendln(obj.toString());
        builder.decLevel().appendln("</object>");

        builder.decLevel().appendln("</Descriptor>");


        return builder.toString();
    }
}
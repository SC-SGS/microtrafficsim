package microtrafficsim.utils.resources;

import java.io.InputStream;
import java.net.URL;


/**
 * A resource which can be accessed as {@link InputStream} or by a {@link URL}.
 *
 * @author Maximilian Luz
 */
public abstract class Resource {

    /**
     * Access this resource as stream.
     *
     * @return an {@link InputStream} constructed from this resource.
     * @throws ResourceException if the resource cannot be accessed.
     */
    public abstract InputStream asStream() throws ResourceException;

    /**
     * Access this resource as URL.
     *
     * @return an {@link URL} to this resource.
     * @throws ResourceException if the resource cannot be accessed.
     */
    public abstract URL toURL() throws ResourceException;


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Resource)) return false;

        try {
            return this.toURL().equals(((Resource) obj).toURL());
        } catch (ResourceException e) { return false; }
    }

    @Override
    public int hashCode() {
        try {
            return this.toURL().hashCode();
        } catch (ResourceException e) {
            return 8161;    // random prime
        }
    }

    /**
     * Return the unique name of this resource, by default this name is based on the URL.
     *
     * @return the unique name of this resource.
     */
    public String getUniqueName() {
        try {
            return toURL().toExternalForm();
        } catch (ResourceException e) { return null; }
    }

    @Override
    public String toString() {
        return getUniqueName();
    }
}

package microtrafficsim.utils.resources;

import microtrafficsim.utils.Streams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * A resource packaged with a java application or library, possibly in a jar-file.
 *
 * @author Maximilian Luz
 */
public class PackagedResource extends Resource {

    private final Class<?> clazz;
    private final String name;

    /**
     * Creates a new packaged resource from the given java resource.
     *
     * @param clazz the class to which the resource is relative.
     * @param name  the name or path of this resource.
     */
    public PackagedResource(Class<?> clazz, String name) {
        this.clazz = clazz;
        this.name  = name;
    }

    @Override
    public URL toURL() throws ResourceException {
        URL url = clazz.getResource(name);

        if (url == null) {
            throw new ResourceException("Resource '" + name + "' (class '" + clazz.getCanonicalName()
                                        + "') not found.");
        }

        return clazz.getResource(name);
    }

    @Override
    public InputStream asStream() throws ResourceException {
        InputStream in = clazz.getResourceAsStream(name);

        if (in == null) {
            throw new ResourceException("Resource '" + name + "' (class '" + clazz.getCanonicalName()
                                        + "') not found.");
        }

        return in;
    }

    /**
     * Create a temporary file from this resource. The file will be deleted after the application exits.
     *
     * @return the temporary file containing the contents of the resource.
     * @throws IOException if the resource cannot be accessed, read from or the temporary file cannot be created or
     * written to.
     */
    public File asTemporaryFile() throws IOException {
        return asTemporaryFile(null);
    }

    /**
     * Create a temporary file from this resource. The file will be deleted after the application exits.
     *
     * @param directory the directory in which the temporary file should be created.
     * @return the temporary file containing the contents of the resource.
     * @throws IOException if the resource cannot be accessed, read from or the temporary file cannot be created or
     * written to.
     */
    public File asTemporaryFile(File directory) throws IOException {
        return asTemporaryFile(directory, true);
    }

    /**
     * Create a temporary file from this resource.
     *
     * @param directory       the directory in which the temporary file should be created.
     * @param deleteAfterExit indicates if the temporary file should be deleted after the application exits.
     * @return the temporary file containing the contents of the resource.
     * @throws IOException if the resource cannot be accessed, read from or the temporary file cannot be created or
     * written to.
     */
    public File asTemporaryFile(File directory, boolean deleteAfterExit) throws IOException {
        try (InputStream in = asStream()) {
            String path = clazz.getResource(name).getPath();

            String fullname = path.substring(path.lastIndexOf('/') + 1);
            int    dot      = fullname.lastIndexOf('.');

            String name;
            String suffix;

            // Note: temp filename must be at least 3 characters long
            if (dot < fullname.length() && dot >= 3) {
                name   = fullname.substring(0, dot);
                suffix = fullname.substring(dot);

            } else if (fullname.length() >= 3) {
                name   = fullname;
                suffix = null;

            } else {
                name   = fullname + ".asFile.";
                suffix = null;
            }

            return Streams.toTemporaryFile(name, suffix, in, directory, deleteAfterExit);
        }
    }
}

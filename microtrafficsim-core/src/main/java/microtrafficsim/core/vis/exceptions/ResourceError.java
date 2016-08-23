package microtrafficsim.core.vis.exceptions;


/**
 * Exception occurring when interacting with resources.
 *
 * @author Maximilian Luz
 */
public class ResourceError extends Error {
    private static final long serialVersionUID = -1431668284337159832L;

    private String resource;

    /**
     * Constructs a new {@code ResourceError}.
     *
     * @param resource the name of the resource.
     */
    public ResourceError(String resource) {
        super("Error accessing resource '" + resource + "'");
        this.resource = resource;
    }

    /**
     * Constructs a new {@code ResourceError}.
     *
     * @param resource the name of the resource.
     * @param cause    the cause of this exception.
     */
    public ResourceError(String resource, Throwable cause) {
        super("Error accessing resource '" + resource + "'", cause);
        this.resource = resource;
    }

    /**
     * Returns the name of the resource causing this exception.
     *
     * @return the name of the resource causing this exception.
     */
    public String getResource() {
        return resource;
    }
}

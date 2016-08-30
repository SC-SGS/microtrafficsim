package microtrafficsim.core.vis;

import java.util.ArrayList;


/**
 * Exception to indicate unsupported OpenGL features.
 *
 * @author Maximilian Luz
 */
public class UnsupportedFeatureException extends Exception {
    private static final long serialVersionUID = 44928915929892712L;

    private ArrayList<String> missing;


    /**
     * Constructs a new {@code UnsupportedFeatureException}, indicating the features supplied via the
     * {@code String} array-list as missing.
     *
     * @param missing the missing features.
     */
    public UnsupportedFeatureException(ArrayList<String> missing) {
        super(buildMessage(missing));
        this.missing = missing;
    }

    /**
     * Builds the string indicating the missing features specified by the given array-list.
     *
     * @param features the missing features.
     * @return the error-message indicating the missing features.
     */
    private static String buildMessage(ArrayList<String> features) {
        StringBuilder sb = new StringBuilder();
        sb.append("Missing OpenGL Features: {");

        if (!features.isEmpty()) {
            for (int i = 0; i < features.size() - 1; i++)
                sb.append(features.get(i)).append(", ");
            sb.append(features.get(features.size() - 1));
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Returns the list of missing features.
     *
     * @return the list of missing features.
     */
    public ArrayList<String> getMissingFeatures() {
        return missing;
    }
}

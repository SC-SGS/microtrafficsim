package microtrafficsim.core.vis;

import java.util.ArrayList;


public class UnsupportedFeatureException extends Exception {
    private static final long serialVersionUID = 44928915929892712L;

    private ArrayList<String> missing;


    public UnsupportedFeatureException(ArrayList<String> missing) {
        super(buildMessage(missing));
        this.missing = missing;
    }

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

    public ArrayList<String> getMissingFeatures() {
        return missing;
    }
}

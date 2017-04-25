package microtrafficsim.ui.gui.utils;

import java.io.File;

/**
 * @author Dominic Parga Cacheiro
 */
public enum FrameTitle {
    DEFAULT, PARSING, LOADING, SAVING;

    public String get() {
        return get(null);
    }

    /**
     * Return the window title
     *
     * @param file File information (e.g. path) is used in the title
     *
     * @return the frame title depending on the given type
     */
    public String get(File file) {
        String frameTitleRaw = "MicroTrafficSim";

        switch (this) {
            case PARSING:
                if (file != null) return frameTitleRaw + " - Parsing [" + file + "]";
                else              return frameTitleRaw + " - Parsing new map, please wait...";
            case LOADING:
                if (file != null) return frameTitleRaw + " - Loading [" + file + "]";
                else              return frameTitleRaw + " - Loading new map, please wait...";
            case SAVING:
                if (file != null) return frameTitleRaw + " - Saving [" + file + "]";
                else              return frameTitleRaw + " - Saving new map, please wait...";
            case DEFAULT:
            default:
                if (file != null) return frameTitleRaw + " - [" + file + "]";
                else              return frameTitleRaw;
        }
    }
}
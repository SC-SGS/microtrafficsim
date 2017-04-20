package microtrafficsim.core.convenience.utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;


public class FileFilters {
    private FileFilters() {}


    public static final FileFilter MAP_OSM_XML = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case "osm": return true;
                default:    return false;
            }
        }

        @Override
        public String getDescription() {
            return "OpenStreetMap XML Files (*.osm)";
        }
    };

    public static final FileFilter MAP_EXFMT = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case "mtsm": return true;
                default:     return false;
            }
        }

        @Override
        public String getDescription() {
            return "MTS ExchangeFormat Map Files (*.mtsm)";
        }
    };

    public static final FileFilter MAP_ALL = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case "mtsm": return true;
                case "osm":  return true;
                default:     return false;
            }
        }

        @Override
        public String getDescription() {
            return "All Map Files (*.mtsm *.osm)";
        }
    };


    private static String extension(File file) {
        String filename = file.getName();
        int i = filename.lastIndexOf('.');

        if (i > 0 && i < filename.length() - 1)
            return filename.substring(i + 1).toLowerCase();
        else
            return "";
    }
}

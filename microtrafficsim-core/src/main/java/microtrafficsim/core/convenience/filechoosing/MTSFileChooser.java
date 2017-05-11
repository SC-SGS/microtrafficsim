package microtrafficsim.core.convenience.filechoosing;

import microtrafficsim.utils.collections.Composite;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class MTSFileChooser extends JFileChooser {

    private Composite<FileFilterSet> fileFilters;

    private FileFilterSet selectedFilter;


    public MTSFileChooser() {
        fileFilters = new Composite<>();
    }


    /**
     * Update the internal selected filter set if not already selected via {@link #selectFilterSet(Class)}
     *
     * @param clazz
     * @param filterSet
     * @param <T>
     */
    public <T extends FileFilterSet> void addFilterSet(Class<T> clazz, T filterSet) {
        fileFilters.set(clazz, filterSet);
        if (this.selectedFilter == null)
            this.selectedFilter = filterSet;
    }

    public void selectFilterSet(Class<? extends FileFilterSet> clazz) {
        selectedFilter = fileFilters.get(clazz);
    }


    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        removeAllChoosableFileFilters();

        for (FileFilter filter : selectedFilter.getSaveFilters())
            super.addChoosableFileFilter(filter);
        if (selectedFilter.addAllSaveFilters())
            super.addChoosableFileFilter(super.getAcceptAllFileFilter());

        super.setFileFilter(selectedFilter.getSelectedSaveFilter());

        int status = super.showSaveDialog(parent);
        selectedFilter.setSelectedSaveFilter(super.getFileFilter());

        return status;
    }

    @Override
    public int showOpenDialog(Component component) throws HeadlessException {
        removeAllChoosableFileFilters();

        for (FileFilter filter : selectedFilter.getOpenFilters())
            super.addChoosableFileFilter(filter);
        if (selectedFilter.addAllOpenFilters())
            super.addChoosableFileFilter(super.getAcceptAllFileFilter());

        super.setFileFilter(selectedFilter.getSelectedOpenFilter());

        int status = super.showOpenDialog(component);
        selectedFilter.setSelectedOpenFilter(super.getFileFilter());

        return status;
    }


    public void removeAllChoosableFileFilters() {
        for (FileFilter filter : super.getChoosableFileFilters()) {
            super.removeChoosableFileFilter(filter);
        }
    }


    /*
    |==========|
    | internal |
    |==========|
    */
    /**
     * @author Maximilian Luz, Dominic Parga Cacheiro
     */
    public static abstract class Filters {

        public static final String MAP_OSM_XML_POSTFIX = "osm";
        public static final String MAP_EXFMT_POSTFIX   = "mtsmap";
        public static final String SCENARIO_POSTFIX    = "mtssim";
        public static final String CONFIG_POSTFIX      = "mtscfg";
        public static final String AREA_POSTFIX        = "mtsarea";
        public static final String ROUTE_POSTFIX       = "mtsroute";


        private Filters() {}


        public static final FileFilter MAP_OSM_XML = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                switch (extension(file)) {
                    case MAP_OSM_XML_POSTFIX: return true;
                    default:                  return false;
                }
            }

            @Override
            public String getDescription() {
                return "OpenStreetMap XML Files (*." + MAP_OSM_XML_POSTFIX + ")";
            }
        };

        public static final FileFilter MAP_EXFMT = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                switch (extension(file)) {
                    case MAP_EXFMT_POSTFIX: return true;
                    default:                return false;
                }
            }

            @Override
            public String getDescription() {
                return "MTS ExchangeFormat Map Files (*." + MAP_EXFMT_POSTFIX + ")";
            }
        };

        public static final FileFilter MAP_ALL = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                switch (extension(file)) {
                    case MAP_EXFMT_POSTFIX:    return true;
                    case MAP_OSM_XML_POSTFIX:  return true;
                    default:                   return false;
                }
            }

            @Override
            public String getDescription() {
                return "All Map Files (*." + MAP_EXFMT_POSTFIX + " *." + MAP_OSM_XML_POSTFIX + ")";
            }
        };


        public static final FileFilter SCENARIO = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                switch (extension(file)) {
                    case SCENARIO_POSTFIX:  return true;
                    default:                return false;
                }
            }

            @Override
            public String getDescription() {
                return "Scenario File (*" + SCENARIO_POSTFIX + ")";
            }
        };


        public static final FileFilter CONFIG = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                switch (extension(file)) {
                    case CONFIG_POSTFIX:  return true;
                    default:              return false;
                }
            }

            @Override
            public String getDescription() {
                return "MTS Config File (*" + CONFIG_POSTFIX + ")";
            }
        };


        public static final FileFilter AREA = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                switch (extension(file)) {
                    case AREA_POSTFIX:  return true;
                    default:            return false;
                }
            }

            @Override
            public String getDescription() {
                return "MTS Area File (*" + AREA_POSTFIX + ")";
            }
        };


        public static final FileFilter ROUTE = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                switch (extension(file)) {
                    case ROUTE_POSTFIX:  return true;
                    default:             return false;
                }
            }

            @Override
            public String getDescription() {
                return "MTS Route File (*" + ROUTE_POSTFIX + ")";
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

    public static abstract class FilterSet implements FileFilterSet {
        private final ArrayList<FileFilter> openFilters;
        private final ArrayList<FileFilter> saveFilters;

        protected FileFilter openSelected;
        protected FileFilter saveSelected;

        protected boolean addAllOpenFilters;
        protected boolean addAllSaveFilters;


        public FilterSet() {
            saveFilters = new ArrayList<>();
            openFilters = new ArrayList<>();

            addAllSaveFilters = true;
            addAllOpenFilters = true;
        }


        @Override
        public boolean addAllSaveFilters() {
            return addAllSaveFilters;
        }

        @Override
        public boolean addAllOpenFilters() {
            return addAllOpenFilters;
        }

        @Override
        public ArrayList<FileFilter> getSaveFilters() {
            return saveFilters;
        }

        @Override
        public ArrayList<FileFilter> getOpenFilters() {
            return openFilters;
        }

        @Override
        public FileFilter getSelectedSaveFilter() {
            return saveSelected;
        }

        @Override
        public void setSelectedSaveFilter(FileFilter filter) {
            saveSelected = filter;
        }

        @Override
        public FileFilter getSelectedOpenFilter() {
            return openSelected;
        }

        @Override
        public void setSelectedOpenFilter(FileFilter filter) {
            openSelected = filter;
        }
    }
}

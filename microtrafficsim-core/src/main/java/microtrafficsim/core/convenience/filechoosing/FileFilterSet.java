package microtrafficsim.core.convenience.filechoosing;

import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public interface FileFilterSet {

    boolean addAllSaveFilters();

    boolean addAllOpenFilters();

    ArrayList<FileFilter> getSaveFilters();

    ArrayList<FileFilter> getOpenFilters();

    FileFilter getSelectedSaveFilter();

    FileFilter getSelectedOpenFilter();

    void setSelectedSaveFilter(FileFilter filter);

    void setSelectedOpenFilter(FileFilter filter);
}

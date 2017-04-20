package microtrafficsim.core.convenience;

import microtrafficsim.core.convenience.utils.FileFilters;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.ArrayList;


public class MapFileChooser extends JFileChooser {

    private ArrayList<FileFilter> saveFilters = new ArrayList<>();
    private ArrayList<FileFilter> openFilters = new ArrayList<>();

    private FileFilter saveSelected;
    private FileFilter openSelected;

    public MapFileChooser() {
        saveFilters.add(FileFilters.MAP_EXFMT);
        saveFilters.add(super.getAcceptAllFileFilter());

        openFilters.add(FileFilters.MAP_ALL);
        openFilters.add(FileFilters.MAP_EXFMT);
        openFilters.add(FileFilters.MAP_OSM_XML);
        openFilters.add(super.getAcceptAllFileFilter());

        saveSelected = saveFilters.get(0);
        openSelected = openFilters.get(0);
    }


    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        removeAllChoosableFileFilters();

        for (FileFilter filter : saveFilters)
            super.addChoosableFileFilter(filter);

        super.setFileFilter(saveSelected);

        int status = super.showSaveDialog(parent);
        saveSelected = super.getFileFilter();

        return status;
    }

    @Override
    public int showOpenDialog(Component component) throws HeadlessException {
        removeAllChoosableFileFilters();

        for (FileFilter filter : openFilters)
            super.addChoosableFileFilter(filter);

        super.setFileFilter(openSelected);

        int status = super.showOpenDialog(component);
        openSelected = super.getFileFilter();

        return status;
    }


    public void removeAllChoosableFileFilters() {
        for (FileFilter filter : super.getChoosableFileFilters()) {
            super.removeChoosableFileFilter(filter);
        }
    }
}
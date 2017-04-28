package microtrafficsim.core.convenience.filechoosing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class MTSFileChooser extends JFileChooser {
    protected ArrayList<FileFilter> saveFilters = new ArrayList<>();
    protected ArrayList<FileFilter> openFilters = new ArrayList<>();

    protected FileFilter saveSelected;
    protected FileFilter openSelected;


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

package microtrafficsim.ui.gui.utils;

import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Dominic Parga Cacheiro
 */
public class UserInteractionUtils {

    public static File askForOpenFile(MTSFileChooser chooser, Component parent) {
        int action = chooser.showOpenDialog(parent);
        if (action == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();

        return null;
    }

    public static File askForSaveFile(MTSFileChooser chooser, String defaultFilename, Component parent) {
        chooser.setSelectedFile(new File(defaultFilename));

        int action = chooser.showSaveDialog(parent);
        if (action == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();

        return null;
    }

    public static boolean isFileOkayForLoading(File file) {
        return file != null;
    }

    public static boolean isFileOkayForSaving(File file, Component parent) {
        if (file == null)
            return false;
        if (file.exists()) {
            return askUserForDecision(
                    "The selected file already exists. Continue?",
                    "Save File",
                    parent);
        }
        return true;
    }


    public static boolean askUserForDecision(String msg, String title, Component parent) {
        int status = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.OK_CANCEL_OPTION);

        return status == JOptionPane.OK_OPTION;
    }

    public static boolean askUserToRemoveScenario(Component parent) {
        return askUserForDecision(
                "To change the origin/destination areas, the currently running scenario has to be removed."
                        + System.lineSeparator()
                        + "Do you still like to change the areas?",
                "Remove currently running scenario?",
                parent
        );
    }


    public static void showRouteResultIsNotDefinedInfo(Component parent) {
        JOptionPane.showMessageDialog(
                parent,
                "There were undefined routes in the loaded route file.\n" +
                        "Thus nothing but the areas is loaded.",
                "Scenario loading failed",
                JOptionPane.INFORMATION_MESSAGE);
    }


    public static void showScenarioLoadingSuccess(Component parent) {
        JOptionPane.showMessageDialog(
                parent,
                "The scenario has been prepared successfully.\n" +
                        "You can start it with 'Space' or 'Arrow →'",
                "Preparation finished successfully",
                JOptionPane.INFORMATION_MESSAGE);
    }


    public static void showLoadingFailure(File chosen, String shortFileDescription, Component parent) {
        JOptionPane.showMessageDialog(
                parent,
                "The chosen file '" + chosen.getName() + "' could not be loaded.\n" +
                        "Please make sure this file exists and is a valid " + shortFileDescription + ".",
                "Error: file could not be loaded",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showSavingSuccess(Component parent) {
        JOptionPane.showMessageDialog(parent,
                "File saved.",
                "Saving file successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showSavingFailure(File file, Component parent) {
        JOptionPane.showMessageDialog(parent,
                "Failed to save file: '" + file.getPath() + "'",
                "Error saving file",
                JOptionPane.ERROR_MESSAGE);
    }
}

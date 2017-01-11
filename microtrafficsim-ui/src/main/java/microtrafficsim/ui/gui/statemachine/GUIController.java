package microtrafficsim.ui.gui.statemachine;

import microtrafficsim.core.vis.input.KeyCommand;

import java.io.File;

/**
 * Represents a controller for (graphical) user input through {@link GUIEvent}s. From a logical view, this class
 * works like a state machine using {@link #transiate(GUIEvent)} to switch its state.
 *
 * @author Dominic Parga Cacheiro
 */
public interface GUIController {

    /**
     * Depending on the given event and its state, this method executes some methods.
     *
     * @param event This event is the input that should be handled
     * @param file The simulation needs an OSM-file to parse and create a street map. This can be done initially by
     *             giving such an OSM-file as parameter here.
     */
    void transiate(GUIEvent event, File file);

    /**
     * Calls {@code transiate(event, file: null)}
     *
     * @see #transiate(GUIEvent, File)
     */
    default void transiate(GUIEvent event) {
        transiate(event, null);
    }

    /**
     * Adds the command and its trigger key event to this controller, so it can pass it through to the GUI
     *
     * @param event e.g. KeyEvent.EVENT_KEY_RELEASED
     * @param vk e.g. KeyEvent.VK_M
     * @param command e.g. e -> System.out.println("M -> KeyEvent.EVENT_KEY_RELEASED")
     */
    void addKeyCommand(short event, short vk, KeyCommand command);
}

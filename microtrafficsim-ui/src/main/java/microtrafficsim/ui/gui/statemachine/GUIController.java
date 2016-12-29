package microtrafficsim.ui.gui.statemachine;

import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.ui.gui.statemachine.impl.GUIEvent;
import microtrafficsim.ui.gui.statemachine.impl.GUIState;

import java.io.File;

/**
 * Represents a controller for (graphical) user input through {@link GUIEvent}s. From a logical view, this class
 * works like a state machine using {@link #transiate(GUIEvent)} to switch {@link GUIState}s.
 *
 * @author Dominic Parga Cacheiro
 */
public interface GUIController {

    /**
     * Depending on the given event, this method executes some methods before it switches its current {@link GUIState}.
     *
     * @param event This event is the input that should be handled
     * @param file The simulation needs an OSM-file to parse and create a street map. This can be done initially by
     *             giving such an OSM-file as parameter here.
     */
    void transiate(GUIEvent event, File file);

    /**
     * @see #transiate(GUIEvent, File)
     */
    default void transiate(GUIEvent event) {
        transiate(event, null);
    }

    /**
     * Adds the command and its trigger key event to this controller, so it can pass it through to the GUI
     *
     * @param event
     * @param vk
     * @param command
     */
    void addKeyCommand(short event, short vk, KeyCommand command);
}

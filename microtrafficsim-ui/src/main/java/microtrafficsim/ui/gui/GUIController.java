package microtrafficsim.ui.gui;

import microtrafficsim.core.vis.input.KeyCommand;

import java.io.File;


/**
 * @author Dominic Parga Cacheiro
 */
public interface GUIController {
    void transiate(GUIEvent event, File file);
    void transiate(GUIEvent event);
    void addKeyCommand(short event, short vk, KeyCommand command);
}

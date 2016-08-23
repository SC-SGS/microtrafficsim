package microtrafficsim.core.vis.input;

import com.jogamp.newt.event.KeyListener;


/**
 * Key-controller capable of handling {@code KeyCommands}.
 *
 * @author Maximilian Luz
 */
public interface KeyController extends KeyListener {

    /**
     * Adds the given {@code KeyCommand} for the given event and key.
     *
     * @param event   the event-type to add the key-command for.
     * @param vk      the key to add the key-command for.
     * @param command the key-command to add.
     * @return the key-command previously associated with the given event and key.
     */
    KeyCommand addKeyCommand(short event, short vk, KeyCommand command);

    /**
     * Removes the given {@code KeyCommand} for the given event and key.
     *
     * @param event the event-type to add the key-command for.
     * @param vk    the key to add the key-command for.
     * @return the key-command previously associated with the given event and key.
     */
    KeyCommand removeKeyCommand(short event, short vk);
}

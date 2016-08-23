package microtrafficsim.core.vis.input;

import com.jogamp.newt.event.KeyEvent;


/**
 * Command to be executed on occurrence of a key-event.
 *
 * @author Maximilian Luz
 */
public interface KeyCommand {

    /**
     * Called to handle a key-event.
     *
     * @param e the {@code KeyEvent} that occurred.
     */
    void event(KeyEvent e);
}

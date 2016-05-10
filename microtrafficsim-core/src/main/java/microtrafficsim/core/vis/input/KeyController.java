package microtrafficsim.core.vis.input;

import com.jogamp.newt.event.KeyListener;


public interface KeyController extends KeyListener {
    KeyCommand addKeyCommand(short event, short vk, KeyCommand command);
    KeyCommand removeKeyCommand(short event, short vk);
}

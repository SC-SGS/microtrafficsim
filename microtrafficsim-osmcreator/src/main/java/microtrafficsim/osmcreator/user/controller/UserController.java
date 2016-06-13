package microtrafficsim.osmcreator.user.controller;

import javafx.scene.input.InputEvent;
import javafx.scene.shape.Shape;

/**
 * @author Dominic Parga Cacheiro
 */
public interface UserController {
  void transiate(UserEvent userEvent, InputEvent inputEvent, Shape clickedShape);
}

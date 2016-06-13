package microtrafficsim.osmcreator.user.gestures.draggable;

import javafx.scene.input.MouseEvent;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Draggable {
  double getDragX();
  void setDragX(double x);
  double getDragY();
  void setDragY(double y);
  default void mouseDidPress(MouseEvent mouseEvent) {

  }
  default void mouseDidRelease(MouseEvent mouseEvent) {

  }
  default void mouseDidDrag(MouseEvent mouseEvent) {

  }
  default void mouseDidEnter(MouseEvent mouseEvent) {

  }
  default void mouseDidExit(MouseEvent mouseEvent) {

  }
}

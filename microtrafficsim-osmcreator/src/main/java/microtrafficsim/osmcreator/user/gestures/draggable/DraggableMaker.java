package microtrafficsim.osmcreator.user.gestures.draggable;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * @author Dominic Parga Cacheiro
 */
public final class DraggableMaker {
  private static class Delta { double x, y; }

  public static <T extends Node & Draggable> void makeDraggable(T draggable) {
    final Delta dragDelta = new Delta();
    draggable.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
      if (mouseEvent.getButton() == MouseButton.PRIMARY) {
        // record a delta distance for the drag and drop operation.
        dragDelta.x = draggable.getDragX() - mouseEvent.getX();
        dragDelta.y = draggable.getDragY() - mouseEvent.getY();
        draggable.getScene().setCursor(Cursor.CLOSED_HAND);
        draggable.mouseDidPress(mouseEvent);
        mouseEvent.consume();
      }
    });
    draggable.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
      if (mouseEvent.getButton() == MouseButton.PRIMARY) {
        draggable.getScene().setCursor(Cursor.HAND);
        draggable.mouseDidRelease(mouseEvent);
        mouseEvent.consume();
      }
    });
    draggable.addEventHandler(MouseEvent.DRAG_DETECTED, mouseEvent -> {
      if (mouseEvent.getButton() == MouseButton.PRIMARY)
        mouseEvent.consume();
    });
    draggable.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
      double newX = mouseEvent.getX() + dragDelta.x;
      if (newX > 0 && newX < draggable.getScene().getWidth()) {
        draggable.setDragX(newX);
      }
      double newY = mouseEvent.getY() + dragDelta.y;
      if (newY > 0 && newY < draggable.getScene().getHeight()) {
        draggable.setDragY(newY);
      }
      draggable.mouseDidDrag(mouseEvent);
      mouseEvent.consume();
    });
    draggable.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> {
      draggable.getScene().setCursor(Cursor.HAND);
      draggable.mouseDidEnter(mouseEvent);
      mouseEvent.consume();
    });
    draggable.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> {
      draggable.getScene().setCursor(Cursor.DEFAULT);
      draggable.mouseDidExit(mouseEvent);
      mouseEvent.consume();
    });
  }
}

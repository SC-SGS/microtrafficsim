package microtrafficsim.osmcreator.user.gestures.selection.impl;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import microtrafficsim.osmcreator.user.gestures.selection.Selectable;
import microtrafficsim.osmcreator.user.gestures.selection.Selection;
import microtrafficsim.osmcreator.user.gestures.selection.SelectionModel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class RubberBandSelection implements Selection {

  public static final double STROKE_WIDTH = 1;
  public static final Color STROKE_COLOR = Color.BLUE;
  public static final Color FILL_COLOR = Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6);

  private Rectangle rect;
  private boolean enabled;
  private DragContext dragContext;
  private Set<Selectable> selectedItems;

  public RubberBandSelection() {
    enabled = false;
    dragContext = new DragContext();
    selectedItems = new HashSet<>();

    rect = new Rectangle(0, 0, 0, 0);
    rect.setStroke(STROKE_COLOR);
    rect.setStrokeWidth(STROKE_WIDTH);
    rect.setStrokeLineCap(StrokeLineCap.ROUND);
    rect.setFill(FILL_COLOR);
  }

  /*
  |===============|
  | (i) Selection |
  |===============|
  */
  @Override
  public Set<Selectable> getSelectedItems() {
    return new HashSet<>(selectedItems);
  }

  @Override
  public void select(Selectable selectable) {
    selectedItems.add(selectable);
    selectable.setSelected(true);
  }

  @Override
  public void unselect(Selectable selectable) {
    selectedItems.remove(selectable);
    selectable.setSelected(false);
  }

  @Override
  public void unselectAll() {
    selectedItems.forEach(selectable -> selectable.setSelected(false));
    selectedItems = new HashSet<>();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void activate(Pane parent, SelectionModel selectionModel) {
    /* press event */
    parent.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
      startSelection(parent, event.getSceneX(), event.getSceneY());
      event.consume();
    });

    /* drag event */
    parent.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
      holdSelection(event.getSceneX(), event.getSceneY());
      event.consume();
    });

    /* release event */
    parent.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
      stopSeleciton(parent, selectionModel);
      event.consume();
    });
  }

  @Override
  public void startSelection(Pane parent, double x, double y) {
    if (enabled)
      return;
    dragContext.mouseAnchorX = x;
    dragContext.mouseAnchorY = y;

    rect.setX(dragContext.mouseAnchorX);
    rect.setY(dragContext.mouseAnchorY);
    rect.setWidth(0);
    rect.setHeight(0);

    parent.getChildren().add(rect);
    enabled = true;
  }

  @Override
  public void holdSelection(double x, double y) {
    if (!enabled)
      return;
    double offsetX = x - dragContext.mouseAnchorX;
    double offsetY = y - dragContext.mouseAnchorY;

    if (offsetX > 0) {
      rect.setWidth(offsetX);
    } else {
      rect.setX(x);
      rect.setWidth(dragContext.mouseAnchorX - rect.getX());
    }
    if (offsetY > 0) {
      rect.setHeight(offsetY);
    } else {
      rect.setY(y);
      rect.setHeight(dragContext.mouseAnchorY - rect.getY());
    }
  }

  @Override
  public void stopSeleciton(Pane parent, SelectionModel selectionModel) {
    for (Selectable selectable : selectionModel.getSelectables()) {
      if (selectable.getBoundsInParent().intersects(rect.getBoundsInParent())) {
        boolean alreadyExists = !selectedItems.add(selectable);
//        if (alreadyExists) {
//          selectedItems.remove(selectable);
//          selectable.setSelected(false);
//        } else {
          selectable.setSelected(true);
//        }
      }
    }
    rect.setX(0);
    rect.setY(0);
    rect.setWidth(0);
    rect.setHeight(0);

    parent.getChildren().remove(rect);
    enabled = false;
  }

  /*
    |=======|
    | stuff |
    |=======|
    */
  private class DragContext {
    double mouseAnchorX, mouseAnchorY;
  }
}

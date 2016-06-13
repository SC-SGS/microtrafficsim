package microtrafficsim.osmcreator.graph;

import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import microtrafficsim.osmcreator.user.controller.UserController;
import microtrafficsim.osmcreator.user.controller.UserEvent;
import microtrafficsim.osmcreator.user.gestures.draggable.Draggable;
import microtrafficsim.osmcreator.user.gestures.draggable.DraggableMaker;
import microtrafficsim.osmcreator.user.gestures.selection.ColoredSelectable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A draggable anchor displayed around a point.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class Crossroad extends Circle implements Draggable, ColoredSelectable {
  private static final double RADIUS = 6;

  private UserController userController;
  private Map<Street, StreetDirection> streets;

  public Crossroad(UserController userController, double x, double y) {
    super(x, y, RADIUS);
    this.userController = userController;
    streets = new HashMap<>();

    setLook();
    DraggableMaker.makeDraggable(this);
  }

  /**
   *
   * @return {@code true} if street didn't exist before
   */
  public boolean add(Street street, final StreetDirection direction) {
    StreetDirection newDirection = direction.merge(streets.get(street));
    return streets.put(street, newDirection) == null;
  }

  void remove(Street street) {
    streets.remove(street);
  }

  public Set<Street> getStreets() {
    return new HashSet<Street>(streets.keySet());
  }

  /*
  |===============|
  | (i) Draggable |
  |===============|
  */
  @Override
  public double getDragX() {
    return getCenterX();
  }

  @Override
  public void setDragX(double x) {
    setCenterX(x);
  }

  @Override
  public double getDragY() {
    return getCenterY();
  }

  @Override
  public void setDragY(double y) {
    setCenterY(y);
  }

  @Override
  public void mouseDidPress(MouseEvent mouseEvent) {
    userController.transiate(UserEvent.CLICK_CROSSROAD, mouseEvent, this);
  }
}

package microtrafficsim.osmcreator.graph;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.shape.Line;
import microtrafficsim.osmcreator.user.controller.UserController;
import microtrafficsim.osmcreator.user.gestures.selection.ColoredSelectable;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.osmcreator.user.controller.UserEvent;
import microtrafficsim.utils.hashing.HashBuilder;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class Street extends Line implements ColoredSelectable {
  private class Binded {
    private DoubleProperty xStartProperty, yStartProperty, xEndProperty, yEndProperty;
    private void bind(DoubleProperty xStart, DoubleProperty yStart, DoubleProperty xEnd, DoubleProperty yEnd) {
      /* origin */
      xStartProperty = new SimpleDoubleProperty();
      yStartProperty = new SimpleDoubleProperty();
      xStartProperty.addListener((observable, oldValue, newValue) -> setStartX(newValue.doubleValue()));
      yStartProperty.addListener((observable, oldValue, newValue) -> setStartY(newValue.doubleValue()));
      xStartProperty.bind(origin.centerXProperty());
      yStartProperty.bind(origin.centerYProperty());
      /* destination */
      xEndProperty = new SimpleDoubleProperty();
      yEndProperty = new SimpleDoubleProperty();
      xEndProperty.addListener((observable, oldValue, newValue) -> setEndX(newValue.doubleValue()));
      yEndProperty.addListener((observable, oldValue, newValue) -> setEndY(newValue.doubleValue()));
      xEndProperty.bind(destination.centerXProperty());
      yEndProperty.bind(destination.centerYProperty());
    }
    public void unbind() {
      xStartProperty.unbind();
      yStartProperty.unbind();
      xEndProperty.unbind();
      yEndProperty.unbind();
    }
  }

  private UserController userController;
  final Crossroad origin, destination;
  private final Binded binded;

  /**
   * NOTE: INCLUSIVE POSITION BINDING!
   * @param userController
   * @param origin
   * @param destination
   */
  public Street(UserController userController, Crossroad origin, Crossroad destination) {
    super(origin.getCenterX(), origin.getCenterY(), destination.getCenterX(), destination.getCenterY());
    this.userController = userController;
    this.origin = origin;
    this.destination = destination;

    setLook();

    binded = new Binded();
    binded.bind(
            origin.centerXProperty(),
            origin.centerYProperty(),
            destination.centerXProperty(),
            destination.centerYProperty());
  }

  public void unbind() {
    binded.unbind();
  }

  public void removeFromCrossroads() {
    origin.remove(this);
    destination.remove(this);
  }

  /*
  |============|
  | (c) Object |
  |============|
  */
  /**
   * @return hash(origin, destination) xor hash(destination, origin), so the order of the crossroads doesn't matter
   */
  @Override
  public int hashCode() {
    HashBuilder hashBuilder = new FNVHashBuilder();
    int hash = hashBuilder.add(origin).add(destination).getHash();
    hashBuilder.reset();
    hash = hash ^ hashBuilder.add(destination).add(origin).getHash();
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Street && hashCode() == obj.hashCode();
  }
}

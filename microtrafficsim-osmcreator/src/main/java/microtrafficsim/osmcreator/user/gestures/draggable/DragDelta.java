package microtrafficsim.osmcreator.user.gestures.draggable;

/**
 * @author Dominic Parga Cacheiro
 */
public class DragDelta {
  public double x, y;

  public DragDelta() {
    this(0, 0);
  }

  public DragDelta(double x, double y) {
    this.x = x;
    this.y = y;
  }
}

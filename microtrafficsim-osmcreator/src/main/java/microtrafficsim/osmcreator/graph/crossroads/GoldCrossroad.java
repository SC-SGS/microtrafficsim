package microtrafficsim.osmcreator.graph.crossroads;

import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.user.controller.UserController;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldCrossroad extends Crossroad {
  private static final int STROKE_WIDTH = 2;
  private static final Color COLOR_UNSEL = Color.GOLD;
  private static final Color COLOR_SEL = Color.ROSYBROWN;

  public GoldCrossroad(UserController userController, double x, double y) {
    super(userController, x, y);
  }

  /*
  |=======================|
  | (i) ColoredSelectable |
  |=======================|
  */
  @Override
  public void setLook() {
    setFill(COLOR_UNSEL);
    setStroke(COLOR_UNSEL);
    setStrokeWidth(STROKE_WIDTH);
    setStrokeType(StrokeType.OUTSIDE);
  }

  @Override
  public void setSelected(boolean selected) {
    Color color = selected ? COLOR_SEL : COLOR_UNSEL;
    setFill(color);
    setStroke(color);
  }
}

package microtrafficsim.osmcreator.graph.crossroads;

import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.user.controller.UserController;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldCrossroad extends Crossroad {

  private boolean isSelected;

  public GoldCrossroad(UserController userController, double x, double y) {
    super(userController, x, y);
    setSelected(false);
  }

  /*
  |=======================|
  | (i) ColoredSelectable |
  |=======================|
  */
  @Override
  public void setLook() {
    setFill(Constants.CROSSROAD_COLOR_UNSEL);
    setStroke(Constants.CROSSROAD_COLOR_UNSEL);
    setStrokeWidth(Constants.CROSSROAD_STROKE_WIDTH);
    setStrokeType(StrokeType.OUTSIDE);
  }

  @Override
  public void setSelected(boolean selected) {
    this.isSelected = selected;
    Color color = selected ? Constants.CROSSROAD_COLOR_SEL : Constants.CROSSROAD_COLOR_UNSEL;
    setFill(color);
    setStroke(color);
  }

  @Override
  public boolean isSelected() {
    return isSelected;
  }
}

package microtrafficsim.osmcreator.graph.streets;

import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.graph.Street;
import microtrafficsim.osmcreator.user.controller.UserController;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldStreet extends Street {
  private static final int STROKE_WIDTH = 3;
  private static final Color COLOR_UNSEL = Color.GOLD;//.deriveColor(1,1,1,0.5);
  private static final Color COLOR_SEL = Color.ROSYBROWN;

  public GoldStreet(UserController userController, Crossroad origin, Crossroad destination) {
    super(userController, origin, destination);
  }

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

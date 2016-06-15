package microtrafficsim.osmcreator.graph.streets;

import com.sun.tools.internal.jxc.ap.Const;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.graph.Street;
import microtrafficsim.osmcreator.user.controller.UserController;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldStreet extends Street {

  private boolean selected;

  public GoldStreet(UserController userController, Crossroad origin, Crossroad destination) {
    super(userController, origin, destination);
    setSelected(false);
  }

  /*
  |=======================|
  | (i) ColoredSelectable |
  |=======================|
  */
  @Override
  public void setLook() {
    setFill(Constants.STREET_COLOR_UNSEL);
    setStroke(Constants.STREET_COLOR_UNSEL);
    setStrokeWidth(Constants.STREET_STROKE_WIDTH);
    setStrokeType(StrokeType.OUTSIDE);
  }

  @Override
  public void setSelected(boolean selected) {
    this.selected = selected;
    Color color = selected ? Constants.STREET_COLOR_SEL : Constants.STREET_COLOR_UNSEL;
    setFill(color);
    setStroke(color);
  }

  @Override
  public boolean isSelected() {
    return selected;
  }
}

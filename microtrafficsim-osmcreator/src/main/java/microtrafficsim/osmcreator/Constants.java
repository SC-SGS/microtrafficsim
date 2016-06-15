package microtrafficsim.osmcreator;

import javafx.scene.paint.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public final class Constants {
  public static final int INITIALZE_SCREEN_WIDTH = 1200;
  public static final int INITIALZE_SCREEN_HEIGHT = 675;
  public static final int PAUSE_TRANSITION_MILLIS = 80;

  /* crossroads */
  public static final int CROSSROAD_STROKE_WIDTH = 2;
  public static final Color CROSSROAD_COLOR_UNSEL = Color.GOLD;
  public static final Color CROSSROAD_COLOR_SEL = Color.ROSYBROWN;

  /* streets */
  public static final int STREET_STROKE_WIDTH = 3;
  public static final Color STREET_COLOR_UNSEL = Color.GOLD;//.deriveColor(1,1,1,0.5);
  public static final Color STREET_COLOR_SEL = Color.ROSYBROWN;
}

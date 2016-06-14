package microtrafficsim.osmcreator.user.controller;

/**
 * @author Dominic Parga Cacheiro
 */
public enum UserEvent {
  CLICK_SCENE,
  CLICK_CROSSROAD,
  MOVE_CROSSROADS,
  FINISHED_MOVING_CROSSROADS,
  START_SELECTION,
  HOLD_SELECTION,
  STOP_SELECTION,
  CLICK_STREET,
  DELETE,
  RIGHT_CLICK




  // NOTE: not up to date!
  /* transitions
   *
   *                       READY ---click scene-------> READY
   *          CROSSROADS_SELECTED ---click scene-------> READY
   *             STREET_SELECTED ---click scene-------> READY
   *              READY_DILIGENT ---click scene-------> CROSSROADS_SELECTED_DILIGENT
   * CROSSROADS_SELECTED_DILIGENT ---click scene-------> CROSSROADS_SELECTED_DILIGENT
   *
   *                       READY ---click crossroad---> CROSSROADS_SELECTED
   *          CROSSROADS_SELECTED ---click crossroad---> CROSSROADS_SELECTED
   *             STREET_SELECTED ---click crossroad---> CROSSROADS_SELECTED
   *              READY_DILIGENT ---click crossroad---> CROSSROADS_SELECTED_DILIGENT
   * CROSSROADS_SELECTED_DILIGENT ---click crossroad---> CROSSROADS_SELECTED_DILIGENT
   *
   *                       READY ---did selection-----> CROSSROADS_SELECTED xor READY
   *          CROSSROADS_SELECTED ---did selection-----> CROSSROADS_SELECTED
   *             STREET_SELECTED ---did selection-----> CROSSROADS_SELECTED xor STREET_SELECTED
   *              READY_DILIGENT ---did selection-----> CROSSROADS_SELECTED_DILIGENT xor READY_DILIGENT
   * CROSSROADS_SELECTED_DILIGENT ---did selection-----> CROSSROADS_SELECTED_DILIGENT
   *
   *                       READY ---click street------> STREET_SELECTED
   *          CROSSROADS_SELECTED ---click street------> STREET_SELECTED
   *             STREET_SELECTED ---click street------> STREET_SELECTED
   *              READY_DILIGENT ---click street------> STREET_SELECTED
   * CROSSROADS_SELECTED_DILIGENT ---click street------> STREET_SELECTED
   *
   *          CROSSROADS_SELECTED ---delete------------> READY
   * CROSSROADS_SELECTED_DILIGENT ---delete------------> READY_DILIGENT
   *             STREET_SELECTED ---delete------------> READY
   *
   *                       READY ---right click-------> READY_DILIGENT
   *          CROSSROADS_SELECTED ---right click-------> CROSSROADS_SELECTED_DILIGENT
   *             STREET_SELECTED ---right click-------> READY_DILIGENT
   *              READY_DILIGENT ---right click-------> READY
   * CROSSROADS_SELECTED_DILIGENT ---right click-------> CROSSROADS_SELECTED
   *
   */
}

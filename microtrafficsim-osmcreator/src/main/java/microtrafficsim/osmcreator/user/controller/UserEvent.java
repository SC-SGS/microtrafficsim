package microtrafficsim.osmcreator.user.controller;

/**
 * @author Dominic Parga Cacheiro
 */
public enum UserEvent {
  CLICK_SCENE,
  CLICK_CROSSROAD,
  DID_SELECTION,
  CLICK_STREET,
  DELETE,
  RIGHT_CLICK

  /* transitions
   *
   *                       READY ---click scene-------> READY
   *          CROSSROAD_SELECTED ---click scene-------> READY
   *             STREET_SELECTED ---click scene-------> READY
   *              READY_DILIGENT ---click scene-------> CROSSROAD_SELECTED_DILIGENT
   * CROSSROAD_SELECTED_DILIGENT ---click scene-------> CROSSROAD_SELECTED_DILIGENT
   *
   *                       READY ---click crossroad---> CROSSROAD_SELECTED
   *          CROSSROAD_SELECTED ---click crossroad---> CROSSROAD_SELECTED
   *             STREET_SELECTED ---click crossroad---> CROSSROAD_SELECTED
   *              READY_DILIGENT ---click crossroad---> CROSSROAD_SELECTED_DILIGENT
   * CROSSROAD_SELECTED_DILIGENT ---click crossroad---> CROSSROAD_SELECTED_DILIGENT
   *
   *                       READY ---did selection-----> CROSSROAD_SELECTED xor READY
   *          CROSSROAD_SELECTED ---did selection-----> CROSSROAD_SELECTED
   *             STREET_SELECTED ---did selection-----> CROSSROAD_SELECTED xor STREET_SELECTED
   *              READY_DILIGENT ---did selection-----> CROSSROAD_SELECTED_DILIGENT xor READY_DILIGENT
   * CROSSROAD_SELECTED_DILIGENT ---did selection-----> CROSSROAD_SELECTED_DILIGENT
   *
   *                       READY ---click street------> STREET_SELECTED
   *          CROSSROAD_SELECTED ---click street------> STREET_SELECTED
   *             STREET_SELECTED ---click street------> STREET_SELECTED
   *              READY_DILIGENT ---click street------> STREET_SELECTED
   * CROSSROAD_SELECTED_DILIGENT ---click street------> STREET_SELECTED
   *
   *          CROSSROAD_SELECTED ---delete------------> READY
   * CROSSROAD_SELECTED_DILIGENT ---delete------------> READY_DILIGENT
   *             STREET_SELECTED ---delete------------> READY
   *
   *                       READY ---right click-------> READY_DILIGENT
   *          CROSSROAD_SELECTED ---right click-------> CROSSROAD_SELECTED_DILIGENT
   *             STREET_SELECTED ---right click-------> READY_DILIGENT
   *              READY_DILIGENT ---right click-------> READY
   * CROSSROAD_SELECTED_DILIGENT ---right click-------> CROSSROAD_SELECTED
   *
   */
}

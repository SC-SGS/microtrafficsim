package microtrafficsim.ui.gui.statemachine;


/**
 * These events can be used as trigger for user input events.
 *
 * @author Dominic Parga Cacheiro
 */
public enum GUIEvent {
    EXIT,
    /* map */
    LOAD_MAP,
    SAVE_MAP,
    /* simulation */
    RUN_SIM,
    RUN_SIM_ONE_STEP,
    PAUSE_SIM,
    NEW_SCENARIO,
    EDIT_SCENARIO,
    CHANGE_AREA_SELECTION,
    /* load/save scenario */
    LOAD_CONFIG,
    SAVE_CONFIG,
    LOAD_ROUTES,
    SAVE_ROUTES,
    LOAD_AREAS,
    SAVE_AREAS,
    /* preferences */
    ACCEPT_PREFS,
    CANCEL_PREFS,
}

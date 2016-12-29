package microtrafficsim.ui.gui.statemachine.impl;


/**
 * These events can be used as trigger for user input events.
 *
 * @author Dominic Parga Cacheiro
 */
public enum GUIEvent {
    CREATE,
    LOAD_MAP,
    DID_PARSE,
    NEW_SIM,
    ACCEPT,
    CANCEL,
    EDIT_SIM,
    RUN_SIM,
    RUN_SIM_ONE_STEP,
    PAUSE_SIM,
    EXIT

    /* transitions:
     *               RAW ---create---------> INIT
     *
     *               RAW ---load map-------> PARSING
     *           SIM_RUN ---load map-------> PARSING_SIM_PAUSE
     *              INIT ---load map-------> PARSING
     *               MAP ---load map-------> PARSING
     *         SIM_PAUSE ---load map-------> PARSING_SIM_PAUSE
     *
     *           PARSING ---did parse------> MAP
     * PARSING_SIM_PAUSE ---did parse------> MAP
     *   PARSING_SIM_RUN ---did parse------> MAP
     *
     *               MAP ---new sim--------> SIM_NEW
     *         SIM_PAUSE ---new sim--------> SIM_NEW
     *           SIM_RUN ---new sim--------> SIM_NEW
     *
     *           SIM_NEW ---accept---------> SIM_PAUSE
     *          EDIT_SIM ---accept---------> SIM_PAUSE
     *
     *           SIM_NEW ---cancel---------> if sim = 1 then SIM_PAUSE else MAP end if;
     *          EDIT_SIM ---cancel---------> SIM_PAUSE
     *
     *         SIM_PAUSE ---edit sim-------> EDIT_SIM
     *           SIM_RUN ---edit sim-------> EDIT_SIM
     *
     *         SIM_PAUSE ---run sim--------> SIM_RUN
     * PARSING_SIM_PAUSE ---run sim--------> PARSING_SIM_RUN
     *
     *         SIM_PAUSE ---run one step---> SIM_PAUSE
     * PARSING_SIM_PAUSE ---run one step---> PARSING_SIM_RUN
     *           SIM_RUN ---run one step---> SIM_PAUSE
     *   PARSING_SIM_RUN ---run one step---> PARSING_SIM_PAUSE
     *
     *           SIM_RUN ---pause sim------> SIM_PAUSE
     *   PARSING_SIM_RUN ---pause sim------> PARSING_SIM_PAUSE
     *
     *            ****** ---exit-----------> EXIT
     */
}

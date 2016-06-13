package microtrafficsim.osmcreator;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.graph.Street;
import microtrafficsim.osmcreator.graph.StreetDirection;
import microtrafficsim.osmcreator.graph.crossroads.GoldCrossroad;
import microtrafficsim.osmcreator.graph.streets.GoldStreet;
import microtrafficsim.osmcreator.user.controller.UserController;
import microtrafficsim.osmcreator.user.controller.UserEvent;
import microtrafficsim.osmcreator.user.controller.UserState;
import microtrafficsim.osmcreator.user.gestures.selection.Selectable;
import microtrafficsim.osmcreator.user.gestures.selection.Selection;
import microtrafficsim.osmcreator.user.gestures.selection.impl.RubberBandSelection;
import microtrafficsim.osmcreator.user.gestures.selection.impl.SelectionModelGroup;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main extends Application implements UserController {

  private final int INITIALZE_SCREEN_WIDTH = 1200;
  private final int INITIALZE_SCREEN_HEIGHT = 675;

  // layout
  private Group streetGroup;
  private SelectionModelGroup crossroadGroup;
  // selection
  private Selection selection;
  // user interaction
  private UserState userState;
  private Street selectedStreet;

  public Main() {
    selection = new RubberBandSelection();
  }

  /*
  |====================|
  | (i) UserController |
  |====================|
  */
  @Override
  public void transiate(UserEvent userEvent, InputEvent inputEvent, Shape clickedShape) {
    System.out.println("UserState before transiate = " + userState);
    System.out.println("UserEvent                  = " + userEvent);
    switch (userEvent) {
      case CLICK_SCENE:
        switch (userState) {
          case READY:
            MouseEvent mouseEvent = (MouseEvent)inputEvent;
            createCrossroad(mouseEvent.getX(), mouseEvent.getY());
            userState = UserState.READY;
            break;
          case CROSSROAD_SELECTED:
            selection.unselectAll();
            userState = UserState.READY;
            break;
          case STREET_SELECTED:
            unselectCurrentStreet();
            userState = UserState.READY;
            break;
          case READY_DILIGENT:
            mouseEvent = (MouseEvent)inputEvent;
            selection.select(createCrossroad(mouseEvent.getX(), mouseEvent.getY()));
            userState = UserState.CROSSROAD_SELECTED_DILIGENT;
            break;
          case CROSSROAD_SELECTED_DILIGENT:
            mouseEvent = (MouseEvent)inputEvent;
            Crossroad destination = createCrossroad(mouseEvent.getX(), mouseEvent.getY());
            createStreetsTo(destination);
            selection.unselectAll();
            selection.select(destination);
            userState = UserState.CROSSROAD_SELECTED_DILIGENT;
            break;
        }
        break;


      case CLICK_CROSSROAD:
        switch (userState) {
          case READY:
            selection.select((Crossroad)clickedShape);
            userState = UserState.CROSSROAD_SELECTED;
            break;
          case CROSSROAD_SELECTED:
            selection.unselectAll();
            selection.select((Crossroad)clickedShape);
            userState = UserState.CROSSROAD_SELECTED;
            break;
          case STREET_SELECTED:
            unselectCurrentStreet();
            selection.select((Crossroad)clickedShape);
            userState = UserState.CROSSROAD_SELECTED;
            break;
          case READY_DILIGENT:
            selection.select((Crossroad)clickedShape);
            userState = UserState.CROSSROAD_SELECTED_DILIGENT;
            break;
          case CROSSROAD_SELECTED_DILIGENT:
            MouseEvent mouseEvent = (MouseEvent)inputEvent;
            createStreetsTo((Crossroad)clickedShape);
            selection.unselectAll();
            selection.select((Crossroad)clickedShape);
            userState = UserState.CROSSROAD_SELECTED_DILIGENT;
            break;
        }
        break;


      case DID_SELECTION:
        switch (userState) {
          case READY:
            if (!selection.getSelectedItems().isEmpty()) {
              userState = UserState.CROSSROAD_SELECTED;
            }
            break;
          case CROSSROAD_SELECTED:
            userState = UserState.CROSSROAD_SELECTED;
            break;
          case STREET_SELECTED:
            if (!selection.getSelectedItems().isEmpty()) {
              unselectCurrentStreet();
              userState = UserState.CROSSROAD_SELECTED;
            }
            break;
          case READY_DILIGENT:
            if (!selection.getSelectedItems().isEmpty()) {
              userState = UserState.CROSSROAD_SELECTED_DILIGENT;
            }
            break;
          case CROSSROAD_SELECTED_DILIGENT:
            userState = UserState.CROSSROAD_SELECTED_DILIGENT;
            break;
        }
        break;


      case CLICK_STREET:
        switch (userState) {
          case READY_DILIGENT:
          case READY:
            select((Street)clickedShape);
            userState = UserState.STREET_SELECTED;
            break;
          case CROSSROAD_SELECTED:
          case CROSSROAD_SELECTED_DILIGENT:
            selection.unselectAll();
            select((Street)clickedShape);
            userState = UserState.STREET_SELECTED;
            break;
          case STREET_SELECTED:
            unselectCurrentStreet();
            select((Street)clickedShape);
            userState = UserState.STREET_SELECTED;
            break;
        }
        break;


      case DELETE:
        switch (userState) {
          case CROSSROAD_SELECTED:
            deleteSelectedCrossroads();
            selection.unselectAll();
            userState = UserState.READY;
            break;
          case STREET_SELECTED:
            deleteSelectedStreet();
            userState = UserState.READY;
            break;
          case CROSSROAD_SELECTED_DILIGENT:
            deleteSelectedCrossroads();
            selection.unselectAll();
            userState = UserState.READY_DILIGENT;
            break;
        }
        break;


      case RIGHT_CLICK:
        switch (userState) {
          case READY:
            userState = UserState.READY_DILIGENT;
            break;
          case CROSSROAD_SELECTED:
            userState = UserState.CROSSROAD_SELECTED_DILIGENT;
            break;
          case STREET_SELECTED:
            unselectCurrentStreet();
            userState = UserState.READY_DILIGENT;
            break;
          case READY_DILIGENT:
            userState = UserState.READY;
            break;
          case CROSSROAD_SELECTED_DILIGENT:
            userState = UserState.CROSSROAD_SELECTED;
            break;
        }
        break;
    }
    System.out.println("UserState after transiate  = " + userState);
  }

  /* crossroads */
  private Crossroad createCrossroad(double x, double y) {
    Crossroad crossroad = new GoldCrossroad(this, x, y);
    crossroadGroup.getChildren().add(crossroad);
    crossroadGroup.add(crossroad);
    return crossroad;
  }

  private void deleteSelectedCrossroads() {
    for (Selectable selectable : selection.getSelectedItems()) {
      Crossroad selectedCrossroad = (Crossroad)selectable;
      /* remove from model */
      Set<Street> streetSet = selectedCrossroad.getStreets();
      streetSet.forEach(Street::removeFromCrossroads);
      /* remove from stage */
      crossroadGroup.getChildren().remove(selectedCrossroad);
      crossroadGroup.remove(selectedCrossroad);
      streetGroup.getChildren().removeAll(streetSet);
    }
  }

  /* streets */
  private void createStreetsTo(Crossroad destination) {
    for (Selectable selectable : selection.getSelectedItems()) {
      Crossroad origin = (Crossroad)selectable;
      createStreet(origin, destination);
    }
  }

  private void createStreet(Crossroad origin, Crossroad destination) {
    Street street = new GoldStreet(this, origin, destination);
    boolean success = origin.add(street, StreetDirection.LEAVING) && destination.add(street, StreetDirection.INCOMING);
    if (success)
      streetGroup.getChildren().add(street);
    else
      street.unbind();
  }

  private void select(Street street) {
    selectedStreet = street;
    selectedStreet.setSelected(true);
  }

  private void unselectCurrentStreet() {
    selectedStreet.setSelected(false);
    selectedStreet = null;
  }

  private void deleteSelectedStreet() {
    /* remove from model */
    selectedStreet.removeFromCrossroads();
    /* remove from stage */
    streetGroup.getChildren().remove(selectedStreet);
    selectedStreet = null;
  }

  /*
  |=================|
  | (c) Application |
  |=================|
  */
  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("MicroTrafficSim - OSM creator");
    Pane root = new Pane();
    crossroadGroup = new SelectionModelGroup();
    streetGroup = new Group();
    root.getChildren().add(crossroadGroup);
    crossroadGroup.getChildren().add(streetGroup);


    /* prepare scene for user input */
    Scene scene = new Scene(root, INITIALZE_SCREEN_WIDTH, INITIALZE_SCREEN_HEIGHT, Color.ALICEBLUE);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
      if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
        transiate(UserEvent.DELETE, keyEvent, null);
        keyEvent.consume();
      }
    });
    PauseTransition holdTimer = new PauseTransition(Duration.millis(100));
    class Wrapper<T> { T content; }
    Wrapper<MouseEvent> wrapper = new Wrapper<>();
    holdTimer.setOnFinished(event -> transiate(UserEvent.CLICK_SCENE, wrapper.content, null));
    scene.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
      switch (mouseEvent.getButton()) {
        case PRIMARY:
          wrapper.content = mouseEvent;
          holdTimer.playFromStart();
          mouseEvent.consume();
          break;
        case SECONDARY:
          transiate(UserEvent.RIGHT_CLICK, mouseEvent, null);
          mouseEvent.consume();
          break;
      }
    });
    scene.addEventHandler(MouseEvent.DRAG_DETECTED, mouseEvent -> {
      holdTimer.stop();
      selection.startSelection(root, mouseEvent.getX(), mouseEvent.getY());
      mouseEvent.consume();
    });
    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
      if (selection.isEnabled()) {
        selection.holdSelection(mouseEvent.getX(), mouseEvent.getY());
        mouseEvent.consume();
      }
    });
    scene.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
      if (selection.isEnabled()) {
        selection.stopSeleciton(root, crossroadGroup);
        transiate(UserEvent.DID_SELECTION, mouseEvent, null);
        mouseEvent.consume();
      }
    });
    scene.setCursor(Cursor.DEFAULT);


    /* show */
    userState = UserState.READY;
    primaryStage.setScene(scene);
    primaryStage.show();
  }



  /*
  |======|
  | main |
  |======|
  */
  public static void main(String[] args) {
    launch(args);
  }
}

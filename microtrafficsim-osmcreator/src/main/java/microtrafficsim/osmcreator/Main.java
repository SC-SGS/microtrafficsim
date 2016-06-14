package microtrafficsim.osmcreator;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
import microtrafficsim.osmcreator.user.gestures.selection.SelectionModel;
import microtrafficsim.osmcreator.user.gestures.selection.impl.RubberBandSelection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main extends Application implements UserController, SelectionModel<Crossroad> {

  // layout
  private Pane root;
  private Group crossroadGroup;
  private Group streetGroup;
  // selection
  private Selection<Crossroad> selection;
  private Set<Crossroad> selectables;
  // user interaction
  private UserState userState;
  private Street selectedStreet;

  public Main() {
    selection = new RubberBandSelection<>();
    selectables = new HashSet<>();
  }

  /*
  |======|
  | main |
  |======|
  */
  public static void main(String[] args) {
    launch(args);
  }

  /*
  |=================|
  | (c) Application |
  |=================|
  */
  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("MicroTrafficSim - OSM creator");
    root = new Pane();
    crossroadGroup = new Group();
    streetGroup = new Group();
    root.getChildren().add(crossroadGroup);
    crossroadGroup.getChildren().add(streetGroup);


    /* prepare crossroad group for dragging groups of crossroads */
    crossroadGroup.addEventHandler(MouseEvent.MOUSE_DRAGGED,
            mouseEvent -> transiate(UserEvent.MOVE_CROSSROADS, mouseEvent, null));
    crossroadGroup.addEventHandler(MouseEvent.MOUSE_RELEASED,
            mouseEvent -> transiate(UserEvent.FINISHED_MOVING_CROSSROADS, mouseEvent, null));


    /* prepare scene for user input */
    Scene scene = new Scene(root, Constants.INITIALZE_SCREEN_WIDTH, Constants.INITIALZE_SCREEN_HEIGHT, Color.ALICEBLUE);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
      if (keyEvent.getCode().equals(KeyCode.BACK_SPACE))
        transiate(UserEvent.DELETE, keyEvent, null);
    });


    PauseTransition clickSceneChiller = new PauseTransition(Duration.millis(Constants.PAUSE_TRANSITION_MILLIS));
    scene.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
      switch (mouseEvent.getButton()) {
        case PRIMARY:
          clickSceneChiller.setOnFinished(event -> transiate(UserEvent.CLICK_SCENE, mouseEvent, null));
          clickSceneChiller.playFromStart();
          break;
        case SECONDARY:
          transiate(UserEvent.RIGHT_CLICK, mouseEvent, null);
          break;
      }
    });
    scene.addEventHandler(MouseEvent.DRAG_DETECTED, mouseEvent -> {
      clickSceneChiller.stop();
      transiate(UserEvent.START_SELECTION, mouseEvent, null);
    });
    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> transiate(UserEvent.HOLD_SELECTION, mouseEvent, null));
    scene.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> transiate(UserEvent.STOP_SELECTION, mouseEvent, null));


    /* show */
    scene.setCursor(Cursor.DEFAULT);
    userState = UserState.READY;
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /*
  |===============================|
  | (i) SelectionModel<Crossroad> |
  |===============================|
  */
  @Override
  public Set<Crossroad> getSelectables() {
    return selectables;
  }

  @Override
  public void add(Crossroad selectable) {
    selectables.add(selectable);
  }

  @Override
  public boolean remove(Crossroad selectable) {
    return selectables.remove(selectable);
  }

  /*
  |====================|
  | (i) UserController |
  |====================|
  */
  @Override
  public void transiate(UserEvent userEvent, InputEvent inputEvent, Shape clickedShape) {
    UserState previousState = userState;

    switch (userEvent) {
      case CLICK_SCENE:
        switch (userState) {
          case READY:
            MouseEvent mouseEvent = (MouseEvent) inputEvent;
            createCrossroad(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.READY;
            break;
          case READY_DILIGENT:
            mouseEvent = (MouseEvent) inputEvent;
            createCrossroad(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.READY_DILIGENT;
            break;
          case SELECTION_ACTIVE:
          case SELECTION_ACTIVE_DILIGENT:
            return;
          case CROSSROADS_SELECTED:
            selection.unselectAll();
            userState = UserState.READY;
            break;
          case CROSSROADS_SELECTED_DILIGENT:
            mouseEvent = (MouseEvent) inputEvent;
            Crossroad newCrossroad = createCrossroad(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            createStreetsTo(newCrossroad);
            selection.unselectAll();
            selection.select(newCrossroad);
            userState = UserState.CROSSROADS_SELECTED_DILIGENT;
            break;
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
            return;
          case STREET_SELECTED:
            unselectCurrentStreet();
            userState = UserState.READY;
            break;
          case STREET_SELECTED_DILIGENT:
            unselectCurrentStreet();
            userState = UserState.READY_DILIGENT;
            break;
        }
        break;


      case CLICK_CROSSROAD:
        Crossroad newCrossroad = (Crossroad)clickedShape;
        switch (userState) {
          case READY:
            selection.select(newCrossroad);
            userState = UserState.CROSSROADS_SELECTED;
            break;
          case READY_DILIGENT:
            selection.select(newCrossroad);
            userState = UserState.CROSSROADS_SELECTED_DILIGENT;
            break;
          case SELECTION_ACTIVE:
          case SELECTION_ACTIVE_DILIGENT:
            return;
          case CROSSROADS_SELECTED:
            if (!newCrossroad.isSelected()) {
              selection.unselectAll();
              selection.select(newCrossroad);
              userState = UserState.CROSSROADS_SELECTED;
            }
            break;
          case CROSSROADS_SELECTED_DILIGENT:
            if (!newCrossroad.isSelected()) {
              createStreetsTo(newCrossroad);
              selection.unselectAll();
              selection.select(newCrossroad);
              userState = UserState.CROSSROADS_SELECTED_DILIGENT;
            }
            break;
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
            return;
          case STREET_SELECTED:
            unselectCurrentStreet();
            selection.select(newCrossroad);
            userState = UserState.CROSSROADS_SELECTED;
            break;
          case STREET_SELECTED_DILIGENT:
            unselectCurrentStreet();
            selection.select(newCrossroad);
            userState = UserState.CROSSROADS_SELECTED_DILIGENT;
            break;
        }
        break;


      case MOVE_CROSSROADS:
        switch (userState) {
          case READY:
          case READY_DILIGENT:
          case SELECTION_ACTIVE:
          case SELECTION_ACTIVE_DILIGENT:
            return;
          case CROSSROADS_SELECTED:
            MouseEvent mouseEvent = (MouseEvent)inputEvent;
            selection.getSelectedItems().forEach(crossroad -> crossroad.updateDragDelta(mouseEvent));
            userState = UserState.MOVING_ACTIVE;
            break;
          case CROSSROADS_SELECTED_DILIGENT:
            mouseEvent = (MouseEvent)inputEvent;
            selection.getSelectedItems().forEach(crossroad -> crossroad.updateDragDelta(mouseEvent));
            userState = UserState.MOVING_ACTIVE_DILIGENT;
            break;
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
            mouseEvent = (MouseEvent)inputEvent;
            selection.getSelectedItems().forEach(crossroad -> crossroad.updateDragPosition(mouseEvent));
            break;
          case STREET_SELECTED:
          case STREET_SELECTED_DILIGENT:
            return;
        }
        break;


      case FINISHED_MOVING_CROSSROADS:
        switch (userState) {
          case READY:
          case READY_DILIGENT:
          case SELECTION_ACTIVE:
          case SELECTION_ACTIVE_DILIGENT:
          case CROSSROADS_SELECTED:
          case CROSSROADS_SELECTED_DILIGENT:
            return;
          case MOVING_ACTIVE:
            userState = UserState.CROSSROADS_SELECTED;
            break;
          case MOVING_ACTIVE_DILIGENT:
            userState = UserState.CROSSROADS_SELECTED_DILIGENT;
            break;
          case STREET_SELECTED:
          case STREET_SELECTED_DILIGENT:
            return;
        }
        break;


      case START_SELECTION:
        switch (userState) {
          case READY:
            MouseEvent mouseEvent = (MouseEvent)inputEvent;
            selection.startSelection(root, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.SELECTION_ACTIVE;
            break;
          case READY_DILIGENT:
            mouseEvent = (MouseEvent)inputEvent;
            selection.startSelection(root, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.SELECTION_ACTIVE_DILIGENT;
            break;
          case SELECTION_ACTIVE:
          case SELECTION_ACTIVE_DILIGENT:
            return;
          case CROSSROADS_SELECTED:
            mouseEvent = (MouseEvent)inputEvent;
            selection.startSelection(root, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.SELECTION_ACTIVE;
            break;
          case CROSSROADS_SELECTED_DILIGENT:
            mouseEvent = (MouseEvent)inputEvent;
            selection.startSelection(root, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.SELECTION_ACTIVE_DILIGENT;
            break;
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
            return;
          case STREET_SELECTED:
            unselectCurrentStreet();
            mouseEvent = (MouseEvent)inputEvent;
            selection.startSelection(root, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.SELECTION_ACTIVE;
            break;
          case STREET_SELECTED_DILIGENT:
            unselectCurrentStreet();
            mouseEvent = (MouseEvent)inputEvent;
            selection.startSelection(root, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            userState = UserState.SELECTION_ACTIVE_DILIGENT;
            break;
        }
        break;


      case HOLD_SELECTION:
        switch (userState) {
          case READY:
          case READY_DILIGENT:
            return;
          case SELECTION_ACTIVE:
          case SELECTION_ACTIVE_DILIGENT:
            MouseEvent mouseEvent = (MouseEvent)inputEvent;
            selection.holdSelection(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            break;
          case CROSSROADS_SELECTED:
          case CROSSROADS_SELECTED_DILIGENT:
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
          case STREET_SELECTED:
          case STREET_SELECTED_DILIGENT:
            return;
        }
        break;


      case STOP_SELECTION:
        switch (userState) {
          case READY:
          case READY_DILIGENT:
            return;
          case SELECTION_ACTIVE:
            selection.stopSelection(root, this);
            if (selection.getSelectedItems().isEmpty())
              userState = UserState.READY;
            else
              userState = UserState.CROSSROADS_SELECTED;
            break;
          case SELECTION_ACTIVE_DILIGENT:
            selection.stopSelection(root, this);
            if (selection.getSelectedItems().isEmpty())
              userState = UserState.READY_DILIGENT;
            else
              userState = UserState.CROSSROADS_SELECTED_DILIGENT;
            break;
          case CROSSROADS_SELECTED:
          case CROSSROADS_SELECTED_DILIGENT:
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
          case STREET_SELECTED:
          case STREET_SELECTED_DILIGENT:
            return;
        }
        break;


      case CLICK_STREET:
        switch (userState) {
          case READY:
            select((Street)clickedShape);
            userState = UserState.STREET_SELECTED;
            break;
          case READY_DILIGENT:
            Street clickedStreet = (Street)clickedShape;
            select(clickedStreet);
            userState = UserState.STREET_SELECTED_DILIGENT;
            break;
          case SELECTION_ACTIVE:
            return;
          case CROSSROADS_SELECTED:
            clickedStreet = (Street)clickedShape;
            selection.unselectAll();
            select(clickedStreet);
            userState = UserState.STREET_SELECTED;
            break;
          case CROSSROADS_SELECTED_DILIGENT:
            clickedStreet = (Street)clickedShape;
            selection.unselectAll();
            select(clickedStreet);
            userState = UserState.STREET_SELECTED_DILIGENT;
            break;
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
            return;
          case STREET_SELECTED:
            clickedStreet = (Street)clickedShape;
            if (!clickedStreet.isSelected()) {
              selection.unselectAll();
              select(clickedStreet);
              userState = UserState.STREET_SELECTED;
            }
            break;
          case STREET_SELECTED_DILIGENT:
            clickedStreet = (Street)clickedShape;
            if (!clickedStreet.isSelected()) {
              selection.unselectAll();
              select(clickedStreet);
              userState = UserState.STREET_SELECTED_DILIGENT;
            }
            break;
        }
        break;


      case DELETE:
        switch (userState) {
          case READY:
          case READY_DILIGENT:
            return;
          case SELECTION_ACTIVE:
          case SELECTION_ACTIVE_DILIGENT:
            return;
          case CROSSROADS_SELECTED:
            deleteSelectedCrossroads();
            selection.unselectAll();
            userState = UserState.READY;
            break;
          case CROSSROADS_SELECTED_DILIGENT:
            deleteSelectedCrossroads();
            selection.unselectAll();
            userState = UserState.READY_DILIGENT;
            break;
          case MOVING_ACTIVE:
          case MOVING_ACTIVE_DILIGENT:
            return;
          case STREET_SELECTED:
            deleteSelectedStreet();
            unselectCurrentStreet();
            userState = UserState.READY;
            break;
          case STREET_SELECTED_DILIGENT:
            deleteSelectedStreet();
            unselectCurrentStreet();
            userState = UserState.READY_DILIGENT;
            break;
        }
        break;


      case RIGHT_CLICK:
        switch (userState) {
          case READY:
            userState = UserState.READY_DILIGENT;
            break;
          case READY_DILIGENT:
            userState = UserState.READY;
            break;
          case SELECTION_ACTIVE:
            userState = UserState.SELECTION_ACTIVE_DILIGENT;
            break;
          case SELECTION_ACTIVE_DILIGENT:
            userState = UserState.SELECTION_ACTIVE;
            break;
          case CROSSROADS_SELECTED:
            userState = UserState.CROSSROADS_SELECTED_DILIGENT;
            break;
          case CROSSROADS_SELECTED_DILIGENT:
            userState = UserState.CROSSROADS_SELECTED;
            break;
          case MOVING_ACTIVE:
            userState = UserState.MOVING_ACTIVE_DILIGENT;
            break;
          case MOVING_ACTIVE_DILIGENT:
            userState = UserState.MOVING_ACTIVE;
            break;
          case STREET_SELECTED:
            userState = UserState.STREET_SELECTED_DILIGENT;
            break;
          case STREET_SELECTED_DILIGENT:
            userState = UserState.STREET_SELECTED;
            break;
        }
        break;
    }
    inputEvent.consume();
    System.out.println(previousState + " ---" + userEvent + "---> " + userState);
  }

  /* crossroads */
  private Crossroad createCrossroad(double x, double y) {
    Crossroad crossroad = new GoldCrossroad(this, x, y);
    crossroad.addEventHandler(MouseEvent.MOUSE_PRESSED,
            mouseEvent -> transiate(UserEvent.CLICK_CROSSROAD, mouseEvent, crossroad));
//    crossroad.addEventHandler(MouseEvent.MOUSE_CLICKED,
//            mouseEvent -> transiate(UserEvent.CLICK_CROSSROAD, mouseEvent, crossroad));
    /* add */
    crossroadGroup.getChildren().add(crossroad);
    selectables.add(crossroad);
    return crossroad;
  }

  private void deleteSelectedCrossroads() {
    Set<Crossroad> selectedItems = selection.getSelectedItems();
    for (Selectable selectable : selectedItems) {
      Crossroad selectedCrossroad = (Crossroad)selectable;
      /* remove from model */
      Set<Street> streetSet = selectedCrossroad.getStreets();
      streetSet.forEach(street -> {
        street.removeFromCrossroads();
        street.unbind();
      });
      /* remove from stage */
      streetGroup.getChildren().removeAll(streetSet);
    }
    crossroadGroup.getChildren().removeAll(selectedItems);
    selectables.removeAll(selectedItems);
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
    if (success) {
      streetGroup.getChildren().add(street);
      street.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> transiate(UserEvent.CLICK_STREET, mouseEvent, street));
    } else
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
    selectedStreet.unbind();
    /* remove from stage */
    streetGroup.getChildren().remove(selectedStreet);
  }
}

package kruscht.ZZZ_core;

import kruscht.draggableTriangle.Anchor;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Dominic Parga Cacheiro
 */
public class ALT_OSMCreator extends Application {

  private final int INITIALZE_SCREEN_WIDTH = 1200;
  private final int INITIALZE_SCREEN_HEIGHT = 675;

  private UserState userState;
  private Group root;
  private Scene scene;

  @Override
  public void start(final Stage stage) throws Exception {
    stage.setTitle("MicroTrafficSim - OSM creator");
    userState = UserState.READY;

    root = new Group();
    scene = new Scene(root, INITIALZE_SCREEN_WIDTH, INITIALZE_SCREEN_HEIGHT, Color.ALICEBLUE);
    setMouseListener();
    scene.setCursor(Cursor.DEFAULT);

    stage.setScene(scene);
    stage.show();
  }

  private void setMouseListener() {
    scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        switch (userState) {
          case READY:
            root.getChildren().add(new Anchor(
                    mouseEvent.getX(),
                    mouseEvent.getY()
            ));
        }
      }
    });
//    scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
//      @Override public void handle(MouseEvent mouseEvent) {
//        getScene().setCursor(Cursor.HAND);
//      }
//    });
//    scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
//      @Override public void handle(MouseEvent mouseEvent) {
//        double newX = mouseEvent.getX() + dragDelta.x;
//        if (newX > 0 && newX < getScene().getWidth()) {
//          setCenterX(newX);
//        }
//        double newY = mouseEvent.getY() + dragDelta.y;
//        if (newY > 0 && newY < getScene().getHeight()) {
//          setCenterY(newY);
//        }
//      }
//    });
//    scene.setOnMouseEntered(new EventHandler<MouseEvent>() {
//      @Override public void handle(MouseEvent mouseEvent) {
//        switch (userState) {
//          case READY:
//            userState = UserState.READY_OVER_NODE;
//        }
//        if (!mouseEvent.isPrimaryButtonDown()) {
//          getScene().setCursor(Cursor.HAND);
//        }
//      }
//    });
//    scene.setOnMouseExited(new EventHandler<MouseEvent>() {
//      @Override public void handle(MouseEvent mouseEvent) {
//        if (!mouseEvent.isPrimaryButtonDown()) {
//          getScene().setCursor(Cursor.DEFAULT);
//        }
//      }
//    });
  }

  public static void main(String[] args) {
    launch(args);
  }
}

package kruscht.draggableTriangle;

import javafx.scene.Scene;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

/** Drag the anchors around to change a polygon's points. */
public class TriangleManipulator extends Application {
  public static void main(String[] args) throws Exception { launch(args); }

  // main application layout logic.
  @Override public void start(final Stage stage) throws Exception {
    Polygon triangle = createStartingTriangle();

    Group root = new Group();
    root.getChildren().add(triangle);
    root.getChildren().addAll(createControlAnchorsFor(triangle.getPoints()));

    stage.setTitle("Triangle Manipulation Sample");
    stage.setScene(
            new Scene(
                    root,
                    400, 400, Color.ALICEBLUE
            )
    );
    stage.show();
  }

  // creates a triangle.
  private Polygon createStartingTriangle() {
    Polygon triangle = new Polygon();

    triangle.getPoints().setAll(
            100d, 100d,
            150d, 50d,
            250d, 150d
    );

    triangle.setStroke(Color.FORESTGREEN);
    triangle.setStrokeWidth(4);
    triangle.setStrokeLineCap(StrokeLineCap.ROUND);
    triangle.setFill(Color.CORNSILK.deriveColor(0, 1.2, 1, 0.6));

    return triangle;
  }

  // @return a list of anchors which can be dragged around to modify points in the format [x1, y1, x2, y2...]
  private ObservableList<Anchor> createControlAnchorsFor(final ObservableList<Double> points) {
    ObservableList<Anchor> anchors = FXCollections.observableArrayList();

    for (int i = 0; i < points.size(); i+=2) {
      final int idx = i;

      DoubleProperty xProperty = new SimpleDoubleProperty(points.get(i));
      DoubleProperty yProperty = new SimpleDoubleProperty(points.get(i + 1));

      xProperty.addListener((ov, oldX, x) -> points.set(idx, x.doubleValue()));
      yProperty.addListener((ov, oldY, y) -> points.set(idx + 1, y.doubleValue()));

      anchors.add(new Anchor(Color.GOLD, xProperty, yProperty));
    }

    return anchors;
  }
}
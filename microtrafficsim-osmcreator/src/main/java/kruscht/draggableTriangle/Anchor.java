package kruscht.draggableTriangle;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

/**
 * A draggable anchor displayed around a point.
 *
 * @author Dominic Parga Cacheiro
 */
public class Anchor extends Circle {

  private static final int STROKE_WIDTH = 2;
  private static final double RADIUS = 8;
  private static final Color COLOR = Color.GOLD;

  private class Delta { double x, y; }

  public Anchor(DoubleProperty x, DoubleProperty y) {
    this(COLOR, x, y);
  }

  public Anchor(Color color, DoubleProperty x, DoubleProperty y) {
    this(color, x.get(), y.get());
    bind(x, y);
  }

  public Anchor(double x, double y) {
    this(COLOR, x, y);
  }

  public Anchor(Color color, double x, double y) {
    super(x, y, RADIUS);
    setFill(color.deriveColor(1, 1, 1, 0.5));
    setStroke(color);
    setStrokeWidth(STROKE_WIDTH);
    setStrokeType(StrokeType.OUTSIDE);
    makeDraggable();
  }

  public void bind(DoubleProperty x, DoubleProperty y) {
    x.bind(centerXProperty());
    y.bind(centerYProperty());
  }

  private void makeDraggable() {
    final Delta dragDelta = new Delta();
    setOnMousePressed(mouseEvent -> {
      // record a delta distance for the drag and drop operation.
      dragDelta.x = getCenterX() - mouseEvent.getX();
      dragDelta.y = getCenterY() - mouseEvent.getY();
      getScene().setCursor(Cursor.CLOSED_HAND);
    });
    setOnMouseReleased(mouseEvent -> {
      getScene().setCursor(Cursor.HAND);
      mouseEvent.consume();
    });
    setOnMouseDragged(mouseEvent -> {
      double newX = mouseEvent.getX() + dragDelta.x;
      if (newX > 0 && newX < getScene().getWidth()) {
        setCenterX(newX);
      }
      double newY = mouseEvent.getY() + dragDelta.y;
      if (newY > 0 && newY < getScene().getHeight()) {
        setCenterY(newY);
      }
    });
    setOnMouseEntered(mouseEvent -> {
      if (!mouseEvent.isPrimaryButtonDown()) {
        getScene().setCursor(Cursor.HAND);
      }
    });
    setOnMouseExited(mouseEvent -> {
      if (!mouseEvent.isPrimaryButtonDown()) {
        getScene().setCursor(Cursor.DEFAULT);
      }
    });
  }
}

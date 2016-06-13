package kruscht.application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import kruscht.com.fxgraph.graph.CellType;
import kruscht.com.fxgraph.graph.Graph;
import kruscht.com.fxgraph.graph.Model;
import kruscht.com.fxgraph.layout.base.Layout;
import kruscht.com.fxgraph.layout.random.RandomLayout;

public class Main extends Application {

  Graph graph;

  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();

    graph = new Graph();

    root.setCenter(graph.getScrollPane());

    Scene scene = new Scene(root, 1024, 768);
    scene.getStylesheets().add(getClass().getResource("osmcreator.css").toExternalForm());
    addGraphComponents();

    Layout layout = new RandomLayout(graph);
    layout.execute();

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void addGraphComponents() {

    Model model = graph.getModel();

    graph.beginUpdate();

    model.addCell("Cell A", CellType.RECTANGLE);
    model.addCell("Cell B", CellType.RECTANGLE);
    model.addCell("Cell C", CellType.RECTANGLE);
    model.addCell("Cell D", CellType.TRIANGLE);
    model.addCell("Cell E", CellType.TRIANGLE);
    model.addCell("Cell F", CellType.RECTANGLE);
    model.addCell("Cell G", CellType.RECTANGLE);

    model.addEdge("Cell A", "Cell B");
    model.addEdge("Cell A", "Cell C");
    model.addEdge("Cell B", "Cell C");
    model.addEdge("Cell C", "Cell D");
    model.addEdge("Cell B", "Cell E");
    model.addEdge("Cell D", "Cell F");
    model.addEdge("Cell D", "Cell G");

    graph.endUpdate();

  }

  public static void main(String[] args) {
    launch(args);
  }
}
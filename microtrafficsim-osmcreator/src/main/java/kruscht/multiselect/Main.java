package kruscht.multiselect;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

    static SelectionModel selectionModel = new SelectionModel();

    DragMouseGestures dragMouseGestures = new DragMouseGestures();

    static Random rnd = new Random();

    @Override
    public void start(Stage primaryStage) {

        Pane pane = new Pane();
        pane.setStyle("-fx-background-color:white");

        new RubberBandSelection( pane);

        double width = 200;
        double height = 160;

        double padding = 20;
        for( int row=0; row < 4; row++) {
            for( int col=0; col < 4; col++) {

                Selectable selectable = new Selectable( width, height);
                selectable.relocate( padding * (col+1) + width * col, padding * (row + 1) + height * row);

                pane.getChildren().add(selectable);

                dragMouseGestures.makeDraggable(selectable);

            }
        }

        Label infoLabel = new Label( "Drag on scene for Rubberband Selection. Shift+Click to add to selection, CTRL+Click to toggle selection. Drag selected nodes for multi-dragging.");
        pane.getChildren().add( infoLabel);

        Scene scene = new Scene( pane, 1600, 900);

        primaryStage.setScene( scene);
        primaryStage.show();        



    }

    public static void main(String[] args) {
        launch(args);
    }
}
package kruscht.multiselect;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class DragMouseGestures {

        final DragContext dragContext = new DragContext();

        private boolean enabled = false;

        public void makeDraggable(final Node node) {

            node.setOnMousePressed(onMousePressedEventHandler);
            node.setOnMouseDragged(onMouseDraggedEventHandler);
            node.setOnMouseReleased(onMouseReleasedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

            // don't do anything if the user is in the process of adding to the selection model
            if( event.isControlDown() || event.isShiftDown())
                return;

            Node node = (Node) event.getSource();

            dragContext.x = node.getTranslateX() - event.getSceneX();
            dragContext.y = node.getTranslateY() - event.getSceneY();

            // clear the model if the current node isn't in the selection => new selection
            if( !Main.selectionModel.contains(node)) {
              Main.selectionModel.clear();
              Main.selectionModel.add( node);
            }

            // flag that the mouse released handler should consume the event, so it won't bubble up to the pane which has a rubberband selection mouse released handler
            enabled = true;

            // prevent rubberband selection handler
            event.consume();
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

            if( !enabled)
                return;

            // all in selection
            for( Node node: Main.selectionModel.selection) {
                node.setTranslateX( dragContext.x + event.getSceneX());
                node.setTranslateY( dragContext.y + event.getSceneY());
            }

        };

        EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {

            // prevent rubberband selection handler
            if( enabled) {

                // set node's layout position to current position,remove translate coordinates
                for( Node node: Main.selectionModel.selection) {
                    fixPosition(node);
                }

                enabled = false;

                event.consume();
            }
        };

        /**
         * Set node's layout position to current position, remove translate coordinates.
         * @param node
         */
        private void fixPosition( Node node) {

            double x = node.getTranslateX();
            double y = node.getTranslateY();

            node.relocate(node.getLayoutX() + x, node.getLayoutY() + y);

            node.setTranslateX(0);
            node.setTranslateY(0);

        }

        class DragContext {

            double x;
            double y;

        }

    }
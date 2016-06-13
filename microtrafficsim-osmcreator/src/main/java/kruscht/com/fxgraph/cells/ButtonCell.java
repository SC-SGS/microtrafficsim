package kruscht.com.fxgraph.cells;

import javafx.scene.control.Button;

import kruscht.com.fxgraph.graph.Cell;

public class ButtonCell extends Cell {

    public ButtonCell(String id) {
        super(id);

        Button view = new Button(id);

        setView(view);

    }

}
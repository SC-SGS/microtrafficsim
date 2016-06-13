package kruscht.com.fxgraph.layout.random;

import java.util.List;
import java.util.Random;

import kruscht.com.fxgraph.graph.Cell;
import kruscht.com.fxgraph.graph.Graph;
import kruscht.com.fxgraph.layout.base.Layout;

public class RandomLayout extends Layout {

    Graph graph;

    Random rnd = new Random();

    public RandomLayout(Graph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Cell> cells = graph.getModel().getAllCells();

        for (Cell cell : cells) {

            double x = rnd.nextDouble() * 500;
            double y = rnd.nextDouble() * 500;

            cell.relocate(x, y);

        }

    }

}
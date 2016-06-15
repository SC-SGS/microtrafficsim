package kruscht.multiselect;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class Selectable extends Region {

  public static Image image = new Image("http://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Siberischer_tiger_de_edit02.jpg/320px-Siberischer_tiger_de_edit02.jpg");
//  public Image image = new Image( getClass().getResource( "tiger.jpg").toExternalForm());

        ImageView view;

        public Selectable( double width, double height) {

            view = new ImageView( image);
            view.setFitWidth(width);
            view.setFitHeight(height);

            getChildren().add( view);

            this.setPrefSize(width, height);
        }

    }
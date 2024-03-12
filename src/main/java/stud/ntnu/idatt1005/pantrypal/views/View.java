package stud.ntnu.idatt1005.pantrypal.views;

import static javafx.stage.Screen.getPrimary;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import stud.ntnu.idatt1005.pantrypal.controllers.Controller;
import stud.ntnu.idatt1005.pantrypal.enums.Route;
import stud.ntnu.idatt1005.pantrypal.utils.ColorPalette;
import stud.ntnu.idatt1005.pantrypal.utils.FontPalette;
import stud.ntnu.idatt1005.pantrypal.views.components.NavBar;


/**
 * This class represents a View in the application. It extends the Scene class from JavaFX and
 * provides a base structure for other views. It has two types of views: HOME and DEFAULT. The HOME
 * view includes a title and a navigation bar. The DEFAULT view only includes a navigation bar.
 */
class View extends Scene {

  private final Route route;

  private final Controller controller;
  /**
   * Root pane of the view.
   */
  BorderPane root;

  /**
   * Constructor for the View class. Initializes the view based on the viewType.
   *
   * @param route The route of the view.
   */
  public View(Controller controller, Route route) {
    super(new BorderPane(), getPrimary().getVisualBounds().getWidth(),
        getPrimary().getVisualBounds().getHeight());
    this.route = route;
    this.controller = controller;
    root = (BorderPane) getRoot();
    this.setFill(ColorPalette.PRIMARY_LIGHT);
    this.view();
  }


  public void view() {
    if (route == Route.HOME) {
      VBox topContainer = new VBox(0);
      topContainer.setAlignment(javafx.geometry.Pos.CENTER);
      Text title = new Text("Pantry Pal");
      title.setFont(FontPalette.HEADER);
      topContainer.getChildren().addAll(
          title,
          new NavBar(controller).getNavBar()
      );
      root.setTop(topContainer);
    } else {
      root.setTop(new NavBar(controller).getNavBar());
    }
  }
}
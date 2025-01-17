package stud.ntnu.idatt1005.pantrypal.views;

import static javafx.stage.Screen.getPrimary;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import stud.ntnu.idatt1005.pantrypal.controllers.CookbookController;
import stud.ntnu.idatt1005.pantrypal.enums.ButtonEnum;
import stud.ntnu.idatt1005.pantrypal.enums.Route;
import stud.ntnu.idatt1005.pantrypal.models.Recipe;
import stud.ntnu.idatt1005.pantrypal.utils.NodeUtils;
import stud.ntnu.idatt1005.pantrypal.utils.Sizing;
import stud.ntnu.idatt1005.pantrypal.utils.SoundPlayer;
import stud.ntnu.idatt1005.pantrypal.views.components.CookbookRecipeComponent;
import stud.ntnu.idatt1005.pantrypal.views.components.StyledButton;


/**
 * The CookbookView class is responsible for creating and managing the view for the
 * cookbook in the application.
 * It extends the View class and uses a CookbookController to interact with the model.
 * The CookbookView displays a collection of recipes in a grid-like structure.
 * The number of recipes per row and the spacing between them can be adjusted.
 * Each recipe is represented by a CookbookRecipeComponent.
 */
public class CookbookView extends View {
  /**
   * The number of recipes per row in the view.
   */
  private static final int RECIPES_PER_ROW = 4;
  /**
   * The spacing between the recipes in the view.
   */
  private final double spacing;
  /**
   * A map containing the recipes to be displayed in the view.
   */
  private final CookbookController controller;

  private final VBox pageContainer;

  /**
   * Constructs a CookbookView with a given CookBookController.
   * It initializes the amount of recipes per row, calculates the spacing between them,
   * and retrieves the recipes from the controller.
   * It then creates the view.
   *
   * @param controller the CookBookController that this view interacts with
   */
  public CookbookView(CookbookController controller) {
    super(controller, Route.COOKBOOK, "/styles/cookbook.css");
    this.controller = controller;
    this.setScrollPane();
    this.pageContainer = new VBox();
    spacing = calculateSpacing();
    addSearchBar();
    render(this.controller.getCurrentSearch());
  }

  /**
   * Adds a search bar to the view.vThe search bar contains a text field for searching
   * recipes and a button for adding a new recipe.
   */
  private void addSearchBar() {
    TextField searchField = createSearchField();
    StyledButton addRecipe = createAddRecipeButton();

    StackPane searchBar = new StackPane();
    searchBar.getChildren().addAll(searchField, addRecipe);

    NodeUtils.addChildren(pageContainer, searchBar);
    searchField.textProperty().addListener((observable, oldValue, newValue) ->
            this.controller.searchRecipes(newValue));
  }

  /**
   * Creates the view for the cookbook.
   * It creates a VBox to contain the rows of recipes, and an HBox for each row.
   * It then adds the CookbookRecipeComponents to the rows and the rows to the container.
   */
  public void render(List<Recipe> currentSearch) {
    VBox recipeContainer = createRecipeContainer(currentSearch);
    if (pageContainer.getChildren().size() < 2) {
      NodeUtils.addChildren(pageContainer, recipeContainer);
    } else {
      pageContainer.getChildren().set(1, recipeContainer);
    }
    getBorderPane().setCenter(pageContainer);
  }

  /**
   * Creates a VBox to contain the rows of recipes.
   * It creates an HBox for each row and adds the CookbookRecipeComponents to the rows.
   *
   * @param currentSearch the list of recipes to be displayed
   * @return the VBox containing the rows of recipes
   */
  private VBox createRecipeContainer(List<Recipe> currentSearch) {
    VBox recipeContainer = new VBox(spacing / 2);
    recipeContainer.setPadding(new Insets(spacing, 0, spacing, 0));

    HBox row = new HBox(spacing);
    ArrayList<Recipe> recipes = new ArrayList<>(currentSearch);
    recipes.sort((a, b) -> Boolean.compare(b.getIsFavorite(), a.getIsFavorite()));
    for (Recipe recipe : recipes) {
      if (row.getChildren().size() >= RECIPES_PER_ROW) {
        row.setAlignment(Pos.CENTER);
        recipeContainer.getChildren().add(row);
        row = new HBox(spacing);
      }
      CookbookRecipeComponent recipeComponent = new CookbookRecipeComponent(recipe);
      recipeComponent.addObserver(controller);
      row.getChildren().add(recipeComponent);
    }

    row.setAlignment(Pos.CENTER);
    recipeContainer.getChildren().add(row);
    return recipeContainer;
  }

  /**
   * Creates a button for adding a new recipe.
   * The button is styled and has an action that notifies the observers of the view.
   *
   * @return the styled button for adding a new recipe
   */
  private StyledButton createAddRecipeButton() {
    StyledButton button = new StyledButton("Add Recipe");
    button.setOnAction(e -> {
      notifyObservers(ButtonEnum.ADD);
      SoundPlayer.playSound(SoundPlayer.Sound.DEFAULT);
    });
    StackPane.setAlignment(button, Pos.CENTER_RIGHT);

    return button;
  }

  /**
   * Creates a text field for searching recipes.
   * The text field is styled and has an action that notifies the observers of the view.
   *
   * @return the styled text field for searching recipes
   */
  private TextField createSearchField() {
    TextField searchField = new TextField();
    searchField.setPromptText("Search");
    NodeUtils.addClasses(searchField, "search-field");
    searchField.setMaxWidth(Sizing.getScreenWidth());
    searchField.setMinWidth(Sizing.getScreenWidth());
    searchField.textProperty().addListener((observable, oldValue, newValue) ->
            this.controller.searchRecipes(newValue));
    return searchField;
  }

  /**
   * Calculates the spacing between the recipes based on the width of the screen,
   * the width of the recipes, and the number of recipes per row.
   *
   * @return the calculated spacing
   */
  private double calculateSpacing() {
    Rectangle2D visualBounds = getPrimary().getVisualBounds();
    return ((visualBounds.getWidth() - RECIPES_PER_ROW * CookbookRecipeComponent.getComponentWidth())
            / (RECIPES_PER_ROW + 1));
  }
}

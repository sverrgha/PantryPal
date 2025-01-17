package stud.ntnu.idatt1005.pantrypal.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import stud.ntnu.idatt1005.pantrypal.PantryPal;
import stud.ntnu.idatt1005.pantrypal.enums.ButtonEnum;
import stud.ntnu.idatt1005.pantrypal.enums.Route;
import stud.ntnu.idatt1005.pantrypal.models.Grocery;
import stud.ntnu.idatt1005.pantrypal.registers.GroceryRegister;
import stud.ntnu.idatt1005.pantrypal.utils.SQL;
import stud.ntnu.idatt1005.pantrypal.utils.ViewManager;
import stud.ntnu.idatt1005.pantrypal.views.ShoppingListView;

/**
 * Controller class for the ShoppingListView.
 * Handles the logic for the ShoppingListView,
 * including managing the grocery register and updating the view.
 * Implements the Observer interface to listen for changes in the view.
 */
public class ShoppingListController extends Controller implements Observer {

  /**
   * The view associated with this controller.
   */
  private final ShoppingListView view;

  /**
   * The register holding the groceries.
   */
  private final GroceryRegister register;

  /**
   * The controller related to the pantry.
   * Used to update the pantry view when groceries are added to the shopping list.
   */
  private final PantryController pantryController;

  /**
   * Constructs a new ShoppingListController with a given view manager
   * and pantry controller. Initializes the grocery register and the shopping list view.
   *
   * @param viewManager The view manager for the application.
   * @param pantryController The controller for the pantry.
   */
  public ShoppingListController(ViewManager viewManager, PantryController pantryController) {
    super(viewManager);
    this.register = new GroceryRegister();
    this.pantryController = pantryController;
    this.view = new ShoppingListView(this);
    this.view.addObserver(this);
    rerender();
    this.viewManager.addView(Route.SHOPPING_LIST, view);

    if (this.isLoggedIn()) {
      this.load();
    }

    rerender();
  }

  /**
   * Retrieve the user's shopping list from the database and adds it to the register.
   * If the user is not logged in, the shopping list is not loaded.
   */
  private void load() {
    String query = "SELECT * FROM shopping_list_grocery WHERE user_name = ?";
    List<Map<String, Object>> groceries = SQL.executeQuery(query, PantryPal.userName);

    for (Map<String, Object> grocery : groceries) {
      String name = grocery.get("grocery_name") != null
          ? grocery.get("grocery_name").toString() : null;
      int quantity = grocery.get("quantity") != null
          ? (int) grocery.get("quantity") : 0;
      String unit = grocery.get("unit") != null
          ? grocery.get("unit").toString() : "g";
      String shelf = grocery.get("shelf_name") != null
          ? grocery.get("shelf_name").toString() : null;
      boolean isBought = grocery.get("is_bought") != null && ((int) grocery.get("is_bought")) != 0;
      if (name != null && unit != null && shelf != null) {
        this.register.addGrocery(new Grocery(name, quantity, unit, shelf, isBought));
      }
    }
  }

  /**
   * Returns the grocery register.
   *
   * @return the grocery register
   */
  public GroceryRegister getRegister() {
    return this.register;
  }

  /**
   * Updates the observer based on the button pressed and the grocery item
   * associated with the action.
   * If the button pressed is ADD, the grocery item is added to the register.
   * If the button pressed is REMOVE, the grocery item is removed from the register.
   * The view is re-rendered after the grocery item is added or removed.
   *
   * @param buttonEnum the button that was pressed
   * @param object     the grocery item associated with the action
   * @throws IllegalArgumentException if the object is not of type Grocery
   */
  @Override
  public void update(ButtonEnum buttonEnum, Object object) {
    if (!(object instanceof Grocery grocery)) {
      throw new IllegalArgumentException("Object is not of type Grocery");
    }
    switch (buttonEnum) {
      case ADD:
        try {
          this.addGrocery(grocery);
          rerender();
          break;
        } catch (IllegalArgumentException e) {
          break;
        }
      case REMOVE:
        try {
          this.removeGrocery(grocery);
          rerender();
          break;
        } catch (IllegalArgumentException e) {
          break;
        }
      default:
        throw new IllegalArgumentException("Button not supported by class");
    }
  }

  /**
   * Updates the observer based on the button pressed.
   * If the button pressed is ADD_TO_PANTRY, the groceries that are checked
   * are added to the pantry and removed from the shopping list.
   * The view is re-rendered after the groceries are added to the pantry.
   *
   * @param buttonEnum the button that was pressed
   */
  @Override
  public void update(ButtonEnum buttonEnum) {
    if (Objects.requireNonNull(buttonEnum) == ButtonEnum.ADD_TO_PANTRY) {
      addGroceriesToPantry();
      rerender();
    } else {
      throw new IllegalArgumentException("Button not supported by class");
    }
  }

  /**
   * Adds groceries to the pantry.
   * The groceries in the grocery register that are checked
   * are added to the pantry and removed from the shopping list.
   */
  public void addGroceriesToPantry() {
    List<Grocery> groceriesToRemove = new ArrayList<>();
    for (Grocery grocery : register.getRegister().values()) {
      if (grocery.getChecked()) {
        pantryController.addGrocery(grocery.getShelf(), grocery.getName(),
                grocery.getQuantity(), grocery.getUnit());
        groceriesToRemove.add(grocery);
      }
    }
    for (Grocery grocery : groceriesToRemove) {
      this.removeGrocery(grocery);
    }
  }

  /**
   * Adds a grocery to the register.
   * If the user is logged in, the grocery is also added to the database.
   *
   * @param grocery the grocery to be added to the register
   */
  public void addGrocery(Grocery grocery) {
    if (grocery == null) {
      throw new IllegalArgumentException("Grocery cannot be null");
    }

    if (register.containsGrocery(grocery.getName())) {
      Grocery oldGrocery = register.getGrocery(grocery.getName());

      int oldAmount = oldGrocery.getQuantity();
      int newAmount = grocery.getQuantity();

      if (this.isLoggedIn()) {
        String query = "UPDATE shopping_list_grocery SET quantity = ? "
            + "WHERE user_name = ? AND grocery_name = ?";
        SQL.executeUpdate(query, oldGrocery.getQuantity() + grocery.getQuantity(),
            PantryPal.userName, grocery.getName());
      }

      oldGrocery.setQuantity(oldAmount + newAmount);
    } else {
      if (this.isLoggedIn()) {
        //Check if grocery exists in grocery table
        String checkGroceryQuery = "SELECT * FROM grocery WHERE name = ?";
        List<Map<String, Object>> groceries =
            SQL.executeQuery(checkGroceryQuery, grocery.getName());
        if (groceries.isEmpty()) {
          String groceryQuery = "INSERT INTO grocery (name, unit) VALUES (?, ?)";
          SQL.executeUpdate(groceryQuery, grocery.getName(), "g");
        }

        //Add grocery to shopping list
        String checkQuery = "SELECT * FROM shopping_list_grocery"
            + " WHERE grocery_name = ? AND user_name = ?";
        List<Map<String, Object>> result =
            SQL.executeQuery(checkQuery, grocery.getName(), PantryPal.userName);

        if (result.isEmpty()) {
          String insertQuery = "INSERT INTO shopping_list_grocery "
              + "(grocery_name, user_name, quantity, is_bought, shelf_name) VALUES (?, ?, ?, ?, ?)";
          SQL.executeUpdate(insertQuery, grocery.getName(), PantryPal.userName,
              grocery.getQuantity(), grocery.getChecked(), grocery.getShelf());
        } else {
          String updateQuery = "UPDATE shopping_list_grocery SET quantity = quantity + ? "
              + "WHERE grocery_name = ? AND user_name = ?";
          SQL.executeUpdate(
              updateQuery, grocery.getQuantity(), grocery.getName(), PantryPal.userName);
        }

      }
      register.addGrocery(grocery);
    }
  }

  /**
   * Removes a grocery from the register.
   * If the user is logged in, the grocery is also removed from the database.
   *
   * @param grocery the grocery to be removed from the register
   */
  private void removeGrocery(Grocery grocery) {
    if (grocery == null) {
      throw new IllegalArgumentException("Grocery cannot be null");
    }

    if (this.isLoggedIn()) {
      String query = "DELETE FROM shopping_list_grocery WHERE user_name = ? AND grocery_name = ?";
      SQL.executeUpdate(query, PantryPal.userName, grocery.getName());
    }

    register.removeGrocery(grocery);
  }

  /**
   * Re-renders the view.
   * Used to update the view with the current grocery register.
   */
  public void rerender() {
    view.render(this.register);
  }
}

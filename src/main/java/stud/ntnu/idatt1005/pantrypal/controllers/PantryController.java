package stud.ntnu.idatt1005.pantrypal.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import stud.ntnu.idatt1005.pantrypal.PantryPal;
import stud.ntnu.idatt1005.pantrypal.enums.ButtonEnum;
import stud.ntnu.idatt1005.pantrypal.enums.Route;
import stud.ntnu.idatt1005.pantrypal.models.Grocery;
import stud.ntnu.idatt1005.pantrypal.models.Shelf;
import stud.ntnu.idatt1005.pantrypal.registers.GroceryRegister;
import stud.ntnu.idatt1005.pantrypal.registers.ShelfRegister;
import stud.ntnu.idatt1005.pantrypal.utils.SQL;
import stud.ntnu.idatt1005.pantrypal.utils.ViewManager;
import stud.ntnu.idatt1005.pantrypal.views.PantryView;

/**
 * Controller for the PantryView.
 * This class is responsible for handling the logic for the PantryView.
 * It is responsible for adding, removing and editing shelves and groceries.
 */
public class PantryController extends Controller implements Observer {

  /**
   * The view for the PantryController.
   */
  private final PantryView view;

  private final ShelfRegister register;

  private int shelfCount = 0;

  /**
   * Constructor for the PantryController.
   *
   * @param viewManager The view manager for the application.
   */
  public PantryController(ViewManager viewManager) {
    super(viewManager);
    this.view = new PantryView(this);
    this.view.addObserver(this);
    this.viewManager.addView(Route.PANTRY, this.view);
    this.register = new ShelfRegister();

    if (this.isLoggedIn()) {
      load(PantryPal.userName);
    }

    rerender();
  }

  /**
   * Loads all shelves and groceries from the database for the specified user.
   *
   * @param username the username of the user to load shelves for.
   */
  private void load(String username) {
    String shelfQuery = "SELECT * FROM pantry_shelf WHERE user_name = ?";
    List<Map<String, Object>> shelves = SQL.executeQuery(shelfQuery, username);

    for (Map<String, Object> shelf : shelves) {
      int shelfId = (int) shelf.get("id");
      String shelfKey = String.valueOf(shelfId);
      String shelfName = shelf.get("name").toString();

      Shelf s = new Shelf(shelfKey, shelfName);

      String groceryQuery = "SELECT g.*, psg.quantity AS quantity FROM pantry_shelf_grocery psg "
          + "INNER JOIN grocery g ON g.name = psg.grocery_name "
          + "WHERE psg.pantry_shelf_id = ?";
      List<Map<String, Object>> groceries = SQL.executeQuery(groceryQuery, shelfId);

      for (Map<String, Object> grocery : groceries) {

        String groceryName = grocery.get("name").toString();
        int groceryQuantity = (int) grocery.get("quantity");
        String groceryUnit = grocery.get("unit").toString();


        Grocery g = new Grocery(groceryName, groceryQuantity, groceryUnit, shelfName, false);
        s.addGrocery(g);
      }
      this.register.addShelf(s);
    }
  }

  /**
   * Returns the register in the controller. In this case, the register is a ShelfRegister.
   *
   * @return the register in the controller
   */
  public ShelfRegister getRegister() {
    return register;
  }

  /**
   * Returns all shelves in the register.
   *
   * @return an array of Shelf objects
   */
  public Shelf[] getShelves() {
    Collection<Shelf> shelves = register.getRegister().values();

    return shelves.toArray(new Shelf[0]);
  }

  /**
   * Updates the observer based on the button pressed and the grocery item associated with the
   * action.
   * If the button pressed is ADD, the grocery item is added to the register and the view is
   * re-rendered.
   * If the button pressed is REMOVE, the grocery item is removed from the register and
   * the view is re-rendered.
   * If the object is not of type Grocery, an IllegalArgumentException is thrown.
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
          addGrocery(grocery.getShelf(), grocery.getName(), grocery.getQuantity(),
                  grocery.getUnit());
          rerender();
          break;
        } catch (IllegalArgumentException e) {
          break;
        }
      case REMOVE:
        try {
          Shelf shelf = register.getShelfByName(grocery.getShelf());
          deleteGrocery(shelf, grocery);
          rerender();
          break;
        } catch (IllegalArgumentException e) {
          break;
        }
      default:
        break;
    }
  }

  /**
   * Updates the observer based on the button pressed.
   * If the button pressed is ADD_TO_PANTRY, the view is re-rendered.
   *
   * @param buttonEnum the button that was pressed
   */
  @Override
  public void update(ButtonEnum buttonEnum) {
    if (Objects.requireNonNull(buttonEnum) == ButtonEnum.ADD_TO_PANTRY) {
      rerender();
    } else {
      throw new IllegalArgumentException("Button not supported by class");
    }
  }

  /**
   * Adds a shelf to the register.
   */
  public void addShelf() {
    shelfCount++;
    String newShelf = "New Shelf " + shelfCount;

    if (isLoggedIn()) {
      String query = "INSERT INTO pantry_shelf (name, user_name) VALUES (?, ?)";
      int id = SQL.executeUpdateWithGeneratedKeys(query,
          newShelf, PantryPal.userName);

      Shelf shelf = new Shelf(String.valueOf(id), newShelf);
      register.addShelf(shelf);
      rerender();

      return;
    }

    Shelf shelf = new Shelf(newShelf);
    register.addShelf(shelf);
    rerender();
  }

  /**
   * Adds a shelf to the register.
   *
   * @param name the name of the shelf
   */
  public Shelf addShelf(String name) {
    if (isLoggedIn()) {
      String query = "INSERT INTO pantry_shelf (name, user_name) VALUES (?, ?)";
      int id = SQL.executeUpdateWithGeneratedKeys(query, name, PantryPal.userName);

      Shelf shelf = new Shelf(String.valueOf(id), name);
      register.addShelf(shelf);
      rerender();

      return shelf;
    }

    Shelf shelf = new Shelf(name);
    register.addShelf(shelf);
    rerender();

    return shelf;
  }

  /**
   * Deletes the specified shelf.
   *
   * @param shelf the shelf to delete
   */
  public void deleteShelf(Shelf shelf) {
    register.removeShelf(shelf);
    rerender();

    if (isLoggedIn()) {
      SQL.executeUpdate("DELETE FROM pantry_shelf WHERE id = ?", shelf.getKey());
    }
  }

  /**
   * Edits the name of the specified shelf.
   *
   * @param shelf the shelf to edit
   * @param name  the new name of the shelf
   */
  public void editShelfName(Shelf shelf, String name) {
    shelf.setName(name);
    rerender();

    if (isLoggedIn()) {
      SQL.executeUpdate("UPDATE pantry_shelf SET name = ? WHERE id = ?", name,
          shelf.getKey());
    }
  }

  /**
   * Returns all groceries from the specified shelf.
   *
   * @param shelf the shelf to get the groceries from
   * @return an array of Grocery objects
   */
  public Grocery[] getGroceries(Shelf shelf) {
    return shelf.getGroceries().values().toArray(new Grocery[0]);
  }

  /**
   * Adds a grocery item to the shelf. If the grocery item already exists in the shelf, the quantity
   * of the grocery item is updated.
   *
   * @param shelf  the shelf to add the grocery item to
   * @param name   the name of the grocery item
   * @param amount the amount of the grocery item
   */
  public void addGrocery(Shelf shelf, String name, int amount, String unit) {
    //Check if grocery already exists in shelf
    if (shelf.getGroceries().containsKey(name)) {
      GroceryRegister groceryRegister = shelf.getGroceryRegister();
      Grocery grocery = groceryRegister.getGrocery(name);
      int oldAmount = grocery.getQuantity();

      if (isLoggedIn()) {
        String groceryQuery = "UPDATE pantry_shelf_grocery SET quantity = ?  "
            + "WHERE pantry_shelf_id = ? AND grocery_name = ?";
        SQL.executeUpdate(groceryQuery, oldAmount + amount, shelf.getKey(), grocery.getName());
      }
      grocery.setQuantity(oldAmount + amount);
    } else {
      if (isLoggedIn()) {
        //Check if grocery exists in grocery table
        String checkGroceryQuery = "SELECT * FROM grocery WHERE name = ?";
        List<Map<String, Object>> groceries = SQL.executeQuery(checkGroceryQuery, name);
        if (groceries.isEmpty()) {
          String groceryQuery = "INSERT INTO grocery (name, unit) VALUES (?, ?)";
          SQL.executeUpdate(groceryQuery, name, unit);
        }
        String shelfGroceryQuery = "INSERT INTO pantry_shelf_grocery "
                + "(pantry_shelf_id, grocery_name, quantity) VALUES (?, ?, ?)";
        SQL.executeUpdate(shelfGroceryQuery, shelf.getKey(), name, amount);

        Grocery grocery = new Grocery(name, amount, unit, shelf.getName(), false);
        shelf.addGrocery(grocery);
      } else {
        Grocery grocery = new Grocery(name, amount, unit, shelf.getName(), false);
        shelf.addGrocery(grocery);

      }
    }
    rerender();
  }

  /**
   * Adds a grocery item to the shelf. If the shelf does not exist in the register, a new shelf is
   * created and the grocery item is added to the shelf.
   *
   * @param shelfName the name of the shelf
   * @param name      the name of the grocery item
   * @param amount    the amount of the grocery item
   */
  public void addGrocery(String shelfName, String name, int amount, String unit) {
    Shelf shelf = null;
    try {
      shelf = register.getShelfByName(shelfName);
    } catch (IllegalArgumentException e) {
      shelf = this.addShelf(shelfName);
    } finally {
      if (shelf == null) {
        shelf = this.addShelf(shelfName);
      }
      this.addGrocery(shelf, name, amount, unit);
    }
  }

  /**
   * Removes the grocery item from the shelf. If the shelf does not exist in the register, an
   * IllegalArgumentException is thrown.
   *
   * @param shelf   the shelf from the grocery is to be removed
   * @param grocery the grocery item to be removed
   */
  public void deleteGrocery(Shelf shelf, Grocery grocery) {
    shelf.removeGrocery(grocery);
    rerender();

    if (isLoggedIn()) {
      String query = "DELETE FROM pantry_shelf_grocery WHERE pantry_shelf_id = ? "
          + "AND grocery_name = ?";
      SQL.executeUpdate(query, shelf.getKey(), grocery.getName());
    }
  }

  /**
   * Renders the view with the updated data.
   */
  public void rerender() {
    view.render(getShelves());
  }
}

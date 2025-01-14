package stud.ntnu.idatt1005.pantrypal.registers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import stud.ntnu.idatt1005.pantrypal.models.Model;

/**
 * This is an abstract class representing a register. It contains a LinkedHashMap where the key is a
 * string and the value is a model. The class provides methods to get the register map, get a model
 * from the register, add a model to the register, and remove a model from the register.
 *
 * @param <T> the type of model that the register will contain
 */
public abstract class Register<T extends Model> {

  LinkedHashMap<String, T> registerMap;

  /**
   * Constructor for Register class. Initializes the register as a new LinkedHashMap.
   */
  protected Register() {
    this.registerMap = new LinkedHashMap<>();
  }

  /**
   * Deep-copy constructor for Register class.
   *
   * @param registerMap the register to be copied
   */
  protected Register(Register<T> registerMap) {
    this.registerMap = new LinkedHashMap<>(registerMap.getRegister());
  }

  /**
   * Abstract method to get the error message.
   *
   * @return the error message
   */
  protected abstract String getErrorMessage();

  /**
   * Get the register map.
   *
   * @return the register map
   */
  public Map<String, T> getRegister() {
    return this.registerMap;
  }

  /**
   * Get a model from the register.
   *
   * @param key the key of the model to be retrieved
   * @return the model with the specified name
   * @throws IllegalArgumentException if the item does not exist in the register
   */
  protected T getModel(String key) throws IllegalArgumentException {
    if (!registerMap.containsKey(key)) {
      throw new IllegalArgumentException(getErrorMessage());
    }
    return registerMap.get(key);
  }

  /**
   * Check if a model exists in the register.
   *
   * @param key the key of the model to be checked
   */
  protected boolean containsModel(String key) {
    return registerMap.containsKey(key);
  }

  /**
   * Add an item to the register.
   *
   * @param model item to be added
   */
  protected void addModel(T model) {
    registerMap.put(model.getKey(), model);
  }

  /**
   * Remove an item from the register.
   *
   * @param model the key of the item to be removed
   * @throws IllegalArgumentException if the item does not exist in the register
   */
  protected void removeModel(T model) throws IllegalArgumentException {
    if (!registerMap.containsKey(model.getKey())) {
      throw new IllegalArgumentException(getErrorMessage());
    }
    registerMap.remove(model.getKey());
  }

  /**
   * Searches for items in the register by name, and searches for all names that
   * contains the search-string.
   *
   * @param search the name of the item to be searched for
   * @return a list of items with the specified name
   */
  protected List<T> searchModels(String search) {
    return registerMap.entrySet().stream()
            .filter(model -> model.getKey().toLowerCase().contains(search.toLowerCase()))
            .map(Map.Entry::getValue)
            .toList();
  }
}

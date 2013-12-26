package liquibase.parser.factory

import liquibase.change.ChangeFactory
import liquibase.change.Change
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by IntelliJ IDEA.
 * User: mbjarland
 * Date: Mar 23, 2009
 * Time: 9:43:14 PM
 *
 * This class is used to register new custom "changes" with the liquibase-dsl package. A custom change is
 * a class which implements liquibase.change.Change and will after registration be usable within the liquibase dsl 
 *
 */
public class OpenChangeFactory {
  private static final OpenChangeFactory instance = new OpenChangeFactory()
  private final Map<String, Class<? extends Change>> tagToClassMap = new ConcurrentHashMap<String, Class<? extends Change>>();
  private final ChangeFactory parentFactory = ChangeFactory.getInstance()

  Logger log = LogFactory.getLogger()
  
  private void GroovyChangeFactory() {}

  public static OpenChangeFactory getInstance() {
    return instance
  }

  public void registerChange(String tagName, Class<? extends Change> changeClass) {
    if (!tagName || tagName.trim().length() == 0) {
      throw new TagRegistrationException("Invalid tag name '${tagName}' provided for class ${changeClass}!")
    }
    
    tagToClassMap.put(tagName, changeClass)
  }

  /**
  * we add an unregister for symmetry
  */
  public void unregisterChange(String tagName) {
    tagToClassMap.remove(tagName)
  }

  public Change create(String tagName) {
    Change result = null;

    Class<? extends Change> clazz = tagToClassMap.get(tagName)
    if (clazz) {
      result = (Change) clazz.newInstance();
    } else {
      //the below will (in the current version...who knows what this might change to) throw a somewhat unhelpful
      //RuntimeException if the tag is not found
      result = parentFactory.create(tagName)
    }

    return result
  }
}
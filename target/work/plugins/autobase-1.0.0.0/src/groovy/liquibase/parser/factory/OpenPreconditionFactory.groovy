package liquibase.parser.factory

import java.util.concurrent.ConcurrentHashMap
import liquibase.precondition.PreconditionFactory
import liquibase.precondition.Precondition

/**
 * Created by IntelliJ IDEA.
 * User: mbjarland
 * Date: Mar 24, 2009
 * Time: 12:34:51 PM
 *
 * This class is used to register new custom preconditions with the liquibase-dsl package. A custom precondition is
 * a class which implements liquibase.precondition.Precondition and will after registration be usable within the liquibase dsl
 *
 */
public class OpenPreconditionFactory {
  private static final OpenPreconditionFactory instance = new OpenPreconditionFactory()
  private final Map<String, Class<? extends Precondition>> tagToClassMap = new ConcurrentHashMap<String, Class<? extends Precondition>>();
  private final PreconditionFactory parentFactory = PreconditionFactory.getInstance()

  private void GroovyChangeFactory() {}

  public static OpenPreconditionFactory getInstance() {
    return instance
  }

  public void registerPrecondition(String tagName, Class<? extends Precondition> preconditionClass) {
    if (!tagName || tagName.trim().length() == 0) {
      throw new TagRegistrationException("Invalid tag name '${tagName}' provided for class ${preconditionClass}!")
    }

    tagToClassMap.put(tagName, preconditionClass)
  }

  /**
   * we add an unregister for symmetry
   */
  public void unregisterPrecondition(String tagName) {
    tagToClassMap.remove(tagName)
  }

  public Precondition create(String tagName) {
    Precondition result = null;

    Class<? extends Precondition> clazz = tagToClassMap.get(tagName)
    if (clazz) {
      result = (Precondition) clazz.newInstance();
    } else {
      //the below will (in the current version...who knows what this might change to) throw a somewhat unhelpful
      //RuntimeException if the tag is not found
      result = parentFactory.create(tagName)
    }

    return result
  }
}
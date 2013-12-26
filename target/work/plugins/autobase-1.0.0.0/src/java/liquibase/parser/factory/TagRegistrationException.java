package liquibase.parser.factory;

/**
 * Created by IntelliJ IDEA.
 * User: mbjarland
 * Date: Mar 24, 2009
 * Time: 12:43:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class TagRegistrationException extends RuntimeException {
  public TagRegistrationException() {
    super();
  }

  public TagRegistrationException(String message) {
    super(message);
  }

  public TagRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TagRegistrationException(Throwable cause) {
    super(cause);
  }
}

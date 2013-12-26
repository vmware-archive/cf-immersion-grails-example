package liquibase.dsl.parser.groovy

import liquibase.change.*
import liquibase.util.Assigner

class ConstraintsCatcher {

  private final ColumnConfig wrapped;

  ConstraintsCatcher(ColumnConfig toWrap) {
    if(!toWrap) { throw new IllegalArgumentException("Needs a change to wrap") }
    wrapped = toWrap;
  }

  void constraints(Map m=[:]) {
    def config = new ConstraintsConfig()
    Assigner.assign(config, m)
    wrapped.setConstraints(config)
  }

  def methodMissing(String name, args) {
    return wrapped.class.metaClass."$name".call(args)
  }

  def propertyMissing(String name) { 
    return wrapped[name]
  }

  def propertyMissing(String name, value) { 
    wrapped[name] = value
  }

}

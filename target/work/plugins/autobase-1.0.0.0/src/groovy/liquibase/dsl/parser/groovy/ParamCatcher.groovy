package liquibase.dsl.parser.groovy

class ParamCatcher {
  private final def wrapped;

  ParamCatcher(toWrap) {
    if(!toWrap) { throw new IllegalArgumentException("Needs a change to wrap") }
    wrapped = toWrap;
  }

  void param(Map m) {
    try {
      wrapped."${m.name}" = m.value
    } catch(Exception e) {
      throw new RuntimeException("Could not assign ${m.name} property value ${m.value} for class ${wrapped.class}", e)
    }
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

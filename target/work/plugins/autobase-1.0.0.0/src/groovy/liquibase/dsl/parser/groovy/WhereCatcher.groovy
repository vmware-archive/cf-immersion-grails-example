package liquibase.dsl.parser.groovy

class WhereCatcher extends groovy.util.Proxy {

  WhereCatcher(toWrap) {
    if(!toWrap) { throw new IllegalArgumentException("Needs a change to wrap") }
    adaptee = toWrap
  }

  void where(String clause) {
    adaptee.whereClause = clause
  }

}

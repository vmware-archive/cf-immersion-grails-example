package liquibase.dsl.parser.groovy

import org.apache.commons.lang.StringUtils
import liquibase.precondition.Conditional

public class PreconditionSupport {
  private Conditional owner

  PreconditionSupport(Conditional owner) {
    this.owner = owner
    owner.setPreconditions(new GroovyPrecondition())
  }


  void preCondition(Map m=null, Closure c) {
    preConditions(m,c)
  }

	void preConditions(Map m=null, Closure c) {
    if(!m) { m = [:] }
    if(!c) { c = {->} }

    def precondition = owner.getPreconditions()
    m.each { k,v ->
      precondition."set${StringUtils.capitalize(k)}"(v)
    }
    precondition.and(c)
	}
}
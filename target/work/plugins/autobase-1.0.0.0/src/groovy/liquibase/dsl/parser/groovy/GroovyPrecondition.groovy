package liquibase.dsl.parser.groovy
//
//    This file is part of Liquibase-DSL.
//
//    Liquibase-DSL is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Lesser General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Liquibase-DSL is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public License
//    along with Liquibase-DSL.  If not, see <http://www.gnu.org/licenses/>.
//
import liquibase.changelog.*;
import liquibase.database.*
import liquibase.exception.*
import liquibase.precondition.*
import liquibase.precondition.core.*
import liquibase.precondition.core.PreconditionContainer;
import liquibase.dsl.parser.groovy.ParamCatcher
import liquibase.parser.factory.OpenPreconditionFactory

/**
* Key class for the preconditions in the Groovy builder.  This class wraps some kind of <code>PreconditionLogic</code>, and
*	delegates the "check" method to it.
*/
class GroovyPrecondition extends PreconditionContainer implements Precondition {

	final PreconditionLogic impl

	/**
	* Convenience constructor that wraps "AndPrecondition".
	*/
	public GroovyPrecondition() {
		this(null);
	}

	/**
	* Constructor that wraps an arbitrary PreconditionLogic implementation.
	*/
	public GroovyPrecondition(PreconditionLogic wrapped) {
    this.impl = wrapped ?: new AndPrecondition()
	}

	String getTagName() { return "groovyPrecondition-" + impl.class.name }

	/**
	* Delegates the implementation of this method to the supplied <code>PreconditionLogic</code>.
	*/
	void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException {
		impl.check(database, changeLog, changeSet)
	}

  void sqlCheck(Map m=[:], String sql) {
    def check = new SqlPrecondition(m)
    check.sql = sql
    impl.addNestedPrecondition(check)
  }

  void customPrecondition(Map m=[:], Closure c = {}) {
   if(!m."className") {
      throw new ChangeLogParseException("Need to specify a class name")
    }
    def custom = Class.forName(m."className").newInstance()
    c.delegate = new ParamCatcher(custom)
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    impl.addNestedPrecondition(new GroovyCustomPreconditionWrapper(custom))
  }

	/**
	*	Method that catches the tags that fall through the cracks.
	*/
  def methodMissing(String name, args) {
    def found = OpenPreconditionFactory.instance.create(name)
		if(found) {
      def stringArg = args?.find { it instanceof String }
      def mapArg = args?.find { it instanceof Map }
      def closureArg = args?.find { it instanceof Closure }
      def precond = found
      if(stringArg) { found.sql = stringArg } 
      if(mapArg) {
        mapArg.each { precond[it.key] = it.value }
      }
      if(closureArg) {
        closureArg.delegate = precond
        closureArg.resolveStrategy = Closure.DELEGATE_FIRST
        closureArg()
      }
      impl.addNestedPrecondition(precond)
		} else {
			throw new ChangeLogParseException("Could not find a precondition matching ${name}")
		}
	}

	/**
	*	Special-case handling of the built-in "and" precondition.
	*/
	void and(Closure body) {
		body.delegate = new GroovyPrecondition(new AndPrecondition())
    body.resolveStrategy = Closure.DELEGATE_FIRST
		body()
		impl.addNestedPrecondition(body.delegate)
	}

	/**
	*	Special-case handling of the built-in "or" precondition.
	*/
	void or(Closure body) {
		body.delegate = new GroovyPrecondition(new OrPrecondition())
    body.resolveStrategy = Closure.DELEGATE_FIRST
		body()
		impl.addNestedPrecondition(body.delegate)
	}

	/**
	* Special-case handling of built-in "not" precondition.
	*/
	void not(Closure body) {
		body.delegate = new GroovyPrecondition(new NotPrecondition())
    body.resolveStrategy = Closure.DELEGATE_FIRST
		body()
		impl.addNestedPrecondition(body.delegate)
	}

	/**
	* Special-case handling of the built-in "dbms" precondition: argument can simply be DBMS type.
	*/
	void dbms(Object[] args) {
		defaulted(new DBMSPrecondition(), ["type"], args)
	}

	/**
	* Special-case handling of the built-in "runningAs" precondition: argument can simply be the username.
	*/
	void runningAs(Object[] args) {
		defaulted(new RunningAsPrecondition(), ["username"], args)
	}

	/**
	*	Special-case handling of the built-in "SQL" precondition: arguments can be expected result, sql.
	*/
	void sql(Object[] args) {
		defaulted(new SqlPrecondition(), ["expectedResult", "sql"], args)
	}

	/**
	* Takes a map of properties, optionally a class, and optionally a closure to execute.  If the class is
	* not provided, the map of properties must include a 'className' property set to the name of the class
	* to be instantiated.  The class loader for the class to be instantiated can be specified with the 
	*	'classLoader' property.  Any other properties are assigned onto the instance of the class.  Once that
	* is all done, the instantiated and populated instance is passed in as the delegate for the closure, so
	* arbitrary code can be executed against it.
	* <p />
	* Once all that is said and done, the instance is added to the nested precondition collection.
	* <p />
	* The class must implement either <code>Precondition</code> or <code>CustomPrecondition</code>.
	*/
	void custom(Map args=[:], Class type=null, Closure body={}) {
		if(!args) { args = [:] }
		if(!type) {
			if(!args['className']) {
				throw new ChangeLogParseException("Custom tag must provide a class or a 'className' attribute")
			} else {
				ClassLoader cl = this.class.classLoader
				if(args['classLoader']) {
					cl = args['classLoader'] as ClassLoader
				}
				try {
					type = Class.forName(args['className'] as String, true, cl)
				} catch(Exception e) { // Apparently popular to fail in Ant
					type = Class.forName(args['className'] as String)
				}
			}
		}
		def inst = type.newInstance()
		args.findAll { it.key != 'className' && it.key != 'classLoader' }?.each { inst[it.key] = it.value }
		if(body) { body.delegate = inst; body.resolveStrategy = Closure.DELEGATE_FIRST; body() }
		if(inst instanceof CustomPrecondition) {
			addNestedPrecondition(new GroovyCustomPreconditionWrapper(inst as CustomPrecondition));
		} else if(inst instanceof Precondition) {
			addNestedPrecondition(inst as Precondition);
		} else {
			throw new ChangeLogParseException("Custom tag class must implement ${CustomPrecondition.class} or ${Precondition.class}: is ${type}")
		}
	}

  public void addNestedPrecondition(Precondition p) {
    impl.addNestedPrecondition(p)
  }


	/**
	*	Handles the common case where we default a certain number of arguments to mapping to certain properties.  The first
	*	argument is the precondition we're populating.  The second is a list of String property names in the order which the
	* arguments are mapped (may not be empty).  The third is the vararg arguments which were passed into the method.
	* <p />
	* This method will check the arguments passed in to guaranty they are the same size as the default property names.  It
	* also handles the case where the user passed a map in to set properties explicitly.
	*/
	protected void defaulted(Precondition p, List defaultPropNames, Object[] args) {
		// TODO Validate preconditions for this method.
		final int propCnt = defaultPropNames.size()
		final def errMsg = { "${p.class.name} requires a map or exactly ${propCnt} arguments (${defaultPropnames.join(", ")})" }
		if(!args) {
			throw new ChangeLogParseException(errMsg())
		} else if(args.size() == 1 && args[0] instanceof Map) {
			args[0].each { p[it.key] = it.value }
		} else if(args.size() == argCnt) {
			defaultPropNames.eachWithIndex { name, idx -> p[name] = args[idx] }
		} else {
			throw new ChangeLogParseException(errMsg())
		}
		impl.addNestedPrecondition(p)
	}

}



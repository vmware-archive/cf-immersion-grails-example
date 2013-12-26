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
import liquibase.*;
import liquibase.change.*;
import liquibase.dsl.properties.*
import liquibase.exception.*
import liquibase.parser.factory.OpenChangeFactory
import liquibase.precondition.Conditional;
import liquibase.precondition.core.PreconditionContainer
import liquibase.resource.ResourceAccessor;
import liquibase.changelog.ChangeSet
import liquibase.dsl.properties.LbdslProperties
/**
* Key class for the change sets in the Groovy builder.  
*/
class GroovyChangeSet extends ChangeSet implements Conditional {
  private @Delegate PreconditionSupport preconditionDelegate = new PreconditionSupport(this)
  String lastTag
  ResourceAccessor resourceAccessor

	/**
	* Convenience constructor for the builder.  If the author is not specified, uses default as provided by <code>LbdslProperties</code>.
	* The optional map may be used to specify "alwaysRun" (def. false), "runOnChange", (def. true), "filePath" (def. physicalFilePath),
	* "context"	(def. empty), and "dbms" (def. empty).
	*/
	public GroovyChangeSet(Map m=[:], String logicalFilePath, String id, String author=null) {
		this(
			id,
			author ?: LbdslProperties.instance.defaultAuthor,
			m?.containsKey('alwaysRun')   ? m.alwaysRun   : false,
			m?.containsKey('runOnChange') ? m.runOnChange : true,
			m?.filePath ?: "",
			m?.context ?: "",
			m?.dbms ?: ""
		)
	}

	/**
	* The constructor for those who want to specify everything themselves.
	*/
	public GroovyChangeSet(String id, String author, boolean alwaysRun, 
				boolean runOnChange, String filePath, String contextList, String dbmsList) 
	{ 
		super(id, author, alwaysRun, runOnChange, filePath, contextList, dbmsList, true)
	} 

  void comment(String cmnt) {
    comments(cmnt);
  }

 	void comments(String cmnt) {
		this.comments = cmnt ?: ""
	}


	void validCheckSum(String chkSum) {
		addValidCheckSum(chkSum)
	}

	void rollbackSql(String sql) {
		addRollBackSQL(sql)
	}

	def methodMissing(String name, args) {
    if(name == "column") {
      throw new IllegalStateException("Either parsed the file wrong or a 'column' tag is out of place (last tag: $lastTag)")
    }
    Change inst = OpenChangeFactory.instance.create(name)
    lastTag = "${inst?.changeMetaData?.name}[${inst?.class}]"
    inst.resourceAccessor = resourceAccessor
		if(args) {
      args = (args as List)
      def stringArg = args.find { it instanceof String }
      def mapArg = args.find { it instanceof Map }
      def closureArg = args.find { it instanceof Closure }
      if(!(new LinkedList(args as List) - [stringArg, mapArg, closureArg]).isEmpty()) {
        def argClasses = args.collect { it?.class }
        throw new ChangeLogParseException("Change arguments may only be one string, one map, and/or one closure.  Was: ${args} (${argClasses})")
      }
      if(stringArg) {
        inst.sql = stringArg
      }
      if(mapArg) {
        mapArg.each { entry -> inst[entry.key] = entry.value }
      } 
      if(closureArg) { 
        closureArg.delegate = inst
        if(inst instanceof ChangeWithColumns 
            || inst.metaClass.methods*.name.any { it.equalsIgnoreCase('addColumn') }
            || inst.metaClass.properties*.name.any { it.equalsIgnoreCase('whereClause') } // TODO Remove this (see below)
        ) {
          closureArg.delegate = new ColumnCatcher(closureArg.delegate)
        } 
/* Proxy of a proxy does not work:  http://jira.codehaus.org/browse/GROOVY-3345
        if(inst.metaClass.properties*.name.any { it.equalsIgnoreCase('whereClause') }) {
          closureArg.delegate = new WhereCatcher(closureArg.delegate)
        }
*/
        closureArg.resolveStrategy = Closure.DELEGATE_FIRST
        closureArg()
      } 
		}
		addChange((Change)inst)
  }

  public String toString() {
    return getId()
  }
}

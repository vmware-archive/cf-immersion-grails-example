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
import grails.util.Environment
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.exception.*
import liquibase.parser.ChangeLogParser
import liquibase.precondition.Conditional;
//import liquibase.dsl.parser.groovy.ConditionallyExecuted
import liquibase.dsl.parser.groovy.PreconditionSupport
import org.apache.commons.io.*
import liquibase.logging.LogFactory
import liquibase.logging.Logger

/**
*	Root of the Groovy database change log builder.
*/
class GroovyDatabaseChangeLog extends DatabaseChangeLog implements Conditional {
  private @Delegate PreconditionSupport preconditionDelegate = new PreconditionSupport(this)
  private static final Logger log = LogFactory.logger

	/**
	*	Convenience method in case a user feels like hand-rolling the change log for some reason.
	*/
	GroovyDatabaseChangeLog(String physicalFilePath, Database db) {
		super(physicalFilePath)
		for (ChangeSet changeSet : this.changeSets) {
			preconditions.check(db, this, changeSet) 
		}
	}

	/**
	* Creates a <code>GroovyChangeSet</code>.
	*/
	void changeSet(Map m=[:], String id = null, String author=null, Closure body) {
    id = id ?: m.id
    if(!id) {
      throw new ChangeLogParseException("Need to specify an id, either as a first String argument or as a named argument")
    }
    author = author ?: m.author
    if(changeSets == null) {
      throw new IllegalStateException("ChangeSets somehow were set to null")
    }
    body.delegate = new GroovyChangeSet(m, logicalFilePath, id, author)
    body.delegate.resourceAccessor = resourceAccessor
    body.resolveStrategy = Closure.DELEGATE_FIRST
    try {
      body()
    } catch(Exception e) {
      e.printStackTrace()
      throw e
    }
    addChangeSet(body.delegate)
	}

  /**
  * Includes all the physical files in the specified directory, including files in subdirectories.  No
  * ordering guatanties are made.
  */
  void includeAll(String dir) {
    if(!dir) {
      throw new ChangeLogParseException("Must provide a directory path to \"includeAll\"")
    }
    try {
      def baselinePattern = /\/baseline-(\w+)\.xml$/
      Enumeration enumer = fileOpener.getResources(dir)
      List resources = Collections.list(enumer)
      resources.each {
        if (it.file ==~ /.*\/\.[^\/]*$/) { 
          log.info("Skipping 'hidden' file ${it}")
        } else if(it.file.endsWith(".groovy")) {
          if ('file'.equalsIgnoreCase(it.protocol)) {
            //see http://jira.codehaus.org/browse/GRAILSPLUGINS-1029 for the reasoning behind the below call
            it = FileUtils.toFile(it).toURI().toURL()
          }

          if(!it.file.endsWith("/changelog.groovy") && !it.file.endsWith(".swp")) {
            log.info("Including file ${it}")
            include(it)
          } else {
            log.fine("Skipping file ${it}")
          }
        } else {
          log.info("Including directory ${it}")
          includeAll(it.file)
        }
      }
    } catch(Exception e) {
      throw new ChangeLogParseException("Groovy includeAll at ${dir} failed", e);
    }
  }

  void include(URL target) {
    if(!target) {
      throw new ChangeLogParseException("Must provide a target to \"include\"")
    }
    include(new BufferedInputStream((InputStream) target.openStream()))
  }

	/**
	*	Includes a file using the <code>FileOpener</code> specified by the <code>fileOpener</code> property.
	*/
	void include(String fileName) {
		if(!fileName) {
			throw new ChangeLogParseException("Must provide a file or filename to \"include\"")
		}
		if(!fileOpener) {
			throw new IllegalStateException("Cannot use #include(String) if the file opener is set to null.");
		}
		try {
		  include(fileOpener.getResourceAsStream(fileName))
	  } catch(Exception e) {
      throw new ChangeLogParseException("Groovy include file at ${fileName} failed", e);
    }
	}

	/**
	*	Includes an <code>InputStream</code>.
	*/
	void include(InputStream stream) {
		try {
      def closure = Eval.me("{ -> \n" + stream.text + "\n}")
      closure.delegate = this
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure()
	  } catch(org.codehaus.groovy.control.CompilationFailedException fail) {
      throw new ChangeLogParseException("Groovy include is not proper Groovy", fail);
    } catch(Exception e) {
      throw new ChangeLogParseException("Unknown error while executing file at ${physicalFilePath}/${logicalFilePath}", e);
    }
	}


  private final Map storage = [:]
  def propertyMissing(String name, value) { 
    return storage[name] = value
  }
  def propertyMissing(String name) { 
    def out = storage[name]
    return out
  }
  void property(Map m) {
    if(m.name) {
      List parts = m.name.split(/\./) as List
      String lastPart = parts.pop()
      Map storeIn = parts.inject(storage) { mapToUse, nextKey ->
        if(!mapToUse[nextKey]) {
          mapToUse[nextKey] = [:]
        } else if(!(mapToUse[nextKey] instanceof Map)) {
          throw new ChangeLogParseException("${m.name} has a parent that is already assigned")
        }
        return mapToUse[nextKey]
      }
      storeIn[lastPart] = m.value
    } else if(m.file) {
      def prop = new Properties()
      prop.load(new BufferedInputStream(new FileInputStream(new File(m.file))))
      prop.entrySet().each { 
        property([name:it.key, value:it.value])
      }
    } else {
      throw new ChangeLogParseException("Property requires either a 'name' or a 'file' to be specified")
    }
  }
}

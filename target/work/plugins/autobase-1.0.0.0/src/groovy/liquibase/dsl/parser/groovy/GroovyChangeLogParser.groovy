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

// added by jun Chen


import grails.util.Environment
import liquibase.changelog.DatabaseChangeLog
import liquibase.resource.ResourceAccessor
import liquibase.database.Database
import liquibase.logging.LogFactory;
import liquibase.parser.ChangeLogParserImpl
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

/**
 *		Provides access to the properties set in the directory denoted by the "lbdsl.home" property.
 */
class GroovyChangeLogParser implements ChangeLogParserImpl {

	private dbChangeLog
    
    List<Class> migrationClasses
    
    def log = LogFactory.getLogger()
	
	public DatabaseChangeLog parse(String physicalChangeLogLocation, 
                                   ResourceAccessor fileOpener, 
                                   Map changeLogProperties, Database db, 
                                   DefaultGrailsApplication app) {

		dbChangeLog = new GroovyDatabaseChangeLog(physicalChangeLogLocation, db);
		dbChangeLog.grailsEnv = Environment.current.name

        this.migrationClasses = app.MigrationClasses
        
        log.debug("Running the following migration classes in this order: " + getSortedMigrations())
        
		getSortedMigrations().each{  
    		if (it.migration) {
                parse it.migration
    		}
		}
            
        return (DatabaseChangeLog)dbChangeLog;
	}  
		    

	void parse(Closure migration) {
		migration.delegate = dbChangeLog
		migration.call()
	}
    
    /**
     * 
     * @return the migrations in the order they should be run.
     * @author Antoine Roux
     */
    List<Class> getSortedMigrations() {

        def remaining = []
        def migrationDependencies = [:]

        // phase 0: find dependencies for each migration
        migrationClasses.each {
            migrationDependencies[it] = findRunAfter(it)
            remaining << it
        }

        def result = [] as LinkedList
        while (!remaining.empty) {
            amendDependentMigrations(migrationDependencies, result, remaining)
        }
        return result
    }

    void amendDependentMigrations(migrationDependencies, result, remaining) {
        def hasAdded = false
        for (Iterator iter=remaining.iterator(); iter.hasNext(); ) {
            def mig = iter.next()
            def dependencies = migrationDependencies[mig]
            if (dependenciesFulfilled(migrationDependencies, result, dependencies)) {
                result << mig
                iter.remove()
                hasAdded = true
            }
        }
        assert hasAdded, "could not resolve migrations, please check for cyclic dependencies, result $result, remaining $remaining"
    }

    /**
     * check if result fulfills all required dependencies
     * @param migrationDependencies
     * @param result
     * @param dependencies
     * @return
     */
    def dependenciesFulfilled(migrationDependencies, result, dependencies) {
        dependencies.every {
            it in result*.originalClass
        }
    }

    /**
     * evaluate the static runAfter field of a migration
     * @param migration
     * @return an array with dependencies
     */
    def findRunAfter(migration) {
        try {
            migration.originalClass.runAfter
        } catch (MissingPropertyException) {
            []
        }
        //if (migration.originalClass.metaClass.hasProperty(instance, "runAfter") ? [] :  migration.originalClass.
    }

}

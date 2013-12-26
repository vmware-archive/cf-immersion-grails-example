package liquibase
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

import liquibase.changelog.DatabaseChangeLog;
import liquibase.resource.ResourceAccessor
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.lockservice.LockService
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.UpdateVisitor
import liquibase.parser.visitor.*;
import liquibase.logging.LogFactory
import liquibase.parser.factory.ChangeLogParserFactory;
import liquibase.dsl.properties.*;
import liquibase.parser.groovy.*;
import liquibase.dsl.parser.groovy.GroovyChangeLogParser
import liquibase.logging.Logger

import org.apache.commons.lang.StringUtils
import java.util.List
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

/**
*  Executes the Liquibase command with appropriate mangling to handle the DSLs.
*/
class LiquibaseDsl extends Liquibase {
  private static final Logger log = LogFactory.logger
	private static final String PARSER_SUFFIX_PREFIX = "lbdsl.parser.suffix"
  private final String changeLogPath;
  private final DefaultGrailsApplication grailApp

	public LiquibaseDsl(String changeLogFile, ResourceAccessor accessor, Database db, DefaultGrailsApplication app ) {
		super(changeLogFile, accessor, db)
    changeLogPath = changeLogFile
	grailApp = app
  
		// Now prep the factory
		ChangeLogParserFactory.register("groovy", GroovyChangeLogParser.class);
		LbdslProperties.instance.pluginParsers.each { 
			ChangeLogParserFactory.register((String)it.key, (String)it.value)
		}
	}

    public void update(String contexts) throws LiquibaseException {
        
        LockService lockService = LockService.getInstance(database);
        lockService.waitForLock();
        
        try {
            
            def parser = new GroovyChangeLogParser(); 	 
            DatabaseChangeLog changeLog = parser.parse(changeLogPath, fileOpener, [:], database, grailApp);
			
			database.checkDatabaseChangeLogTable(false, changeLog, new String[0]);
            changeLog.validate(database);
            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog, 
                            [
                                new ShouldRunChangeSetFilter(database),
                                new ContextChangeSetFilter(contexts),
                                new DbmsChangeSetFilter(database)
                            ]
                            as ChangeSetFilter[]);
            logIterator.run(new UpdateVisitor(database), database);
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                log.severe("Could not release lock", e);
            }
        }
    }


}

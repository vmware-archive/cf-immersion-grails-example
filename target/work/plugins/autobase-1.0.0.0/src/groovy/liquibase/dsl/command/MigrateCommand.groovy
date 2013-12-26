package liquibase.dsl.command
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
import liquibase.dsl.properties.*;
import liquibase.resource.FileSystemResourceAccessor
import liquibase.database.Database;
import liquibase.dsl.command.api.LbdslCommand;
import liquibase.logging.LogFactory;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

/**
*		Executes the migration against the database.
*/
class MigrateCommand implements LbdslCommand  {

	private final def log = LogFactory.getLogger();

	void exec(final List<String> args) {
		def myArgs = new LinkedList(args);
		def logFile = args.isEmpty() ? promptLogFile() : args.remove(0);
		logFile = new File(logFile)
		validateFile(logFile)
		Database db = LbdslProperties.instance.database
		new LiquibaseDsl(logFile.getAbsolutePath(), new FileSystemResourceAccessor(), db).update(null);
	}

	private static String promptLogFile() {
		print "What database change log file should I migrate? >"
		return StringUtils.chomp(System.in.withReader { r -> r.readLine(); })
	}

	private static void validateFile(File logFile) {
		if(logFile == null) { throw new NullArgumentException("logFile") }
		if(!logFile.exists()) {
			throw new IllegalArgumentException("File $logFile does not exist");
		}
		if(!logFile.isFile()) {
			throw new IllegalArgumentException("Path $logFile does not denote a simple file");
		}
		if(!logFile.canRead())  {
			throw new IllegalArgumentException("Cannot read file at $logFile");
		}
	}

}

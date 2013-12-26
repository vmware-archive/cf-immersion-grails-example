package autobase.change;

import java.util.Set;

import liquibase.*;
import liquibase.statement.*;
import liquibase.statement.core.RawSqlStatement
import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.CheckSum;
import liquibase.changelog.*;
import liquibase.database.*;
import liquibase.database.structure.*;
import liquibase.database.sql.*;
import liquibase.sql.visitor.*; 
import liquibase.exception.*;
import liquibase.resource.ResourceAccessor;
import groovy.sql.*;
import org.codehaus.groovy.control.*;
import org.w3c.dom.*;
import org.apache.commons.lang.StringUtils

public class GroovyScriptChange extends AbstractSQLChange implements liquibase.change.Change {
  static final String TAG_NAME = 'groovyScript'
  String sourceFile;
  Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>(1);
  def result;

  ResourceAccessor resourceAccessor;
  ChangeSet changeSet;

  String getChangeName() { return TAG_NAME }
  String getTagName() { return this.changeName }

  void executeStatements(Database db, List<SqlVisitor> sqlVisitors) {
    if(!sourceFile) {
      throw new IllegalStateException(changeName + " tag requires 'sourceFile' property to be set")
    }
    String logicalName = sourceFile
    logicalName = StringUtils.remove(logicalName, '.groovy')
    logicalName = StringUtils.remove(logicalName, '.')
    logicalName = logicalName + ".groovy"
    def binding = new Binding()
    binding.setVariable("db", db)
    binding.setVariable("sql", new Sql(db.connection.underlyingConnection) )
    binding.setVariable("fileOpener", resourceAccessor)
    binding.setVariable("changeSet", changeSet)
    binding.setVariable("sourceFile", sourceFile)
    binding.setVariable("logicalName", logicalName)
    def shell = new GroovyShell(binding)
    result = shell.evaluate(resourceAccessor.getResourceAsStream(sourceFile).text, logicalName)
  }

  void saveStatements(Database db, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException,  StatementNotSupportedOnDatabaseException {
    writer << (db.lineComment + " Insert arbitrary Groovy processing from ${sourceFile} here")
  }

  void executeRollbackStatements(Database database, List<SqlVisitor> sqlVisitors) {
    throw new RollbackImpossibleException("Can't roll back arbitrary Groovy code")
  }

  void saveRollbackStatement(Database database, List<SqlVisitor> sqlVisitors, java.io.Writer writer)
                           throws java.io.IOException,
                                  UnsupportedChangeException,
                                  RollbackImpossibleException,
                                  StatementNotSupportedOnDatabaseException {
    throw new RollbackImpossibleException("Can't roll back arbitrary Groovy code")
  }

  SqlStatement[] generateStatements(Database database) {
    return ([new RawSqlStatement(database.lineComment + " Insert arbitrary Groovy processing from ${sourceFile} here")] as SqlStatement[])
  }

  SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
    throw new RollbackImpossibleException("Can't roll back arbitrary Groovy code")
  }

  boolean canRollBack() { false }

  String getConfirmationMessage() { "Executed Groovy in ${sourceFile}" }

  org.w3c.dom.Node createNode(Document currentChangeLogDOM) {
    def element = currentChangeLogDOM.createElement(changeName)
    setAttribute("sourceFile", sourceFile)
    return element
  }

  String getMD5Sum() {
    return liquibase.util.MD5Util.computeMD5(resourceAccessor.getResourceAsStream(sourceFile))
  }

  void setUp() throws SetupException {}

  Set<DatabaseObject> getAffectedDatabaseObjects() { return affectedObjects }

  ValidationErrors validate(Database database) {
    if(!sourceFile) {
		def error = new ValidationErrors()
		error.addError("No source file provided")
		return error
    }
    if(!resourceAccessor.getResourceAsStream(sourceFile)) {
		def error = new ValidationErrors()
		error.addError("Could not open ${sourceFile} for some unknown reason")
		return error
    }
  }
  

	@Override
	public boolean supportsRollback(Database arg0) {
		return false;
	}

}



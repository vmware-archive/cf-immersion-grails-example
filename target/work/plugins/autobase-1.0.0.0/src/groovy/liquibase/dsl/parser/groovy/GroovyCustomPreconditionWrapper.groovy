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

/**
*	A wrapper for custom preconditions as they're used by the Groovy parser.
*/
class GroovyCustomPreconditionWrapper implements Precondition {

	private final CustomPrecondition precondition;

	public GroovyCustomPreconditionWrapper(CustomPrecondition toWrap) {
		precondition = toWrap
	}

	@Override
	void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
		precondition.check(database)
	}

	String getTagName() { return "custom-${precondition.class.name}" }
	
	@Override
	String getName() { return getTagName() }

	@Override
	public ValidationErrors validate(Database db) {
		return new ValidationErrors();
	}

	@Override
	public Warnings warn(Database arg0) {
		return new Warnings()
	}


}

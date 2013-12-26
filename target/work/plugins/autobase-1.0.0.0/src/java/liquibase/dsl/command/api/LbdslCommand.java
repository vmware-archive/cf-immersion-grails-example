package liquibase.dsl.command.api;
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


import java.util.List;

/**
* Interface that must be implemented for a Liquibase-DSL Command to be run.  The class must also provide a no-arg constructor.
*/
public interface LbdslCommand {
	/**
	* Responsible for performing the actual command execution.  Will be provided a List of String arguments.
	*/
	public void exec(List<String> args);
}

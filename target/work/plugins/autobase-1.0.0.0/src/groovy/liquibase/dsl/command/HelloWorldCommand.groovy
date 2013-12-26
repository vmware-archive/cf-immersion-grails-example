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

import liquibase.dsl.command.api.LbdslCommand;
import liquibase.dsl.properties.LbdslProperties;

class HelloWorldCommand implements LbdslCommand  {

	void exec(final List<String> args) {
		println "Hello, world!"
    println "Default author is: ${new LbdslProperties().defaultAuthor}"
	}

}

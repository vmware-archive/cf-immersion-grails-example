package liquibase.dsl.command.exec
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

import liquibase.dsl.command.FindCommand

class RunCommand {

	static void main(final String[] args) {
		if(args.length < 1) {
			throw new IllegalArgumentException("USAGE: RunCommand command-name [arg1 [arg2 [arg3 ...")
		}
		def myArgs = args as List;

		//LbdslProperties.instance.properties.store(System.out, "Properties loaded by Liquibase-DSL")

		Class runClass = new FindCommand().find(myArgs.remove(0))
		def cmd = runClass.newInstance()
		cmd.exec(myArgs)
	}

}

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

import liquibase.dsl.properties.*;

class FindCommand {

	/*
	static void main(final String[] args) {
		if(args.length < 1) {
			throw new IllegalArgumentException("USAGE: FindCommand command-name")
		}
		def cmd = args[0];
		//cmd = (cmd.replaceAll(/-\w/) { (it[1] as String).toUpperCase() }) + "Command";
		//cmd = new StringBuffer(cmd)
		//cmd[0] = cmd[0].toUpperCase()
		//cmd = cmd.toString()

		Class runClass = new FindCommand().find(cmd) { LbdslProperties.getProperties() }
		if(runClass) {
			println runClass.toString();
			System.exit(0);
		} else {
			System.exit(-1);
		}
	}
	*/

	Class find(String cmd) {

		Class out = findClass("liquibase.dsl.command", cmd)
		if(out) {
			return out;
		}
		
		def pkgs = LbdslProperties.instance.commandPackages
		while(pkgs) {
			def tryPkg = pkgs.remove(0)
			out = findClass(tryPkg, cmd)
			if(out) {
				return out
			}
		}

		return null
	}

	private Class findClass(String pkg, String cmd) {
		try {
			return Class.forName(pkg + "." + cmd)
		} catch(ClassNotFoundException cnfe) {
			return null
		}
	}

}

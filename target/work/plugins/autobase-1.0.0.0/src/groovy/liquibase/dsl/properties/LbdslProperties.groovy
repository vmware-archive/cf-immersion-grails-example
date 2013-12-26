package liquibase.dsl.properties
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
import java.sql.*
import liquibase.database.*

/**
*		Provides access to the properties set in the directory denoted by the "lbdsl.home" property.
*/
class LbdslProperties {

	static final String SUFFIX_PARSER_PROPERTY = "lbdsl.parser.suffix"
	static final String COMMAND_PACKAGE_PROPERTY = "lbdsl.packages.cmd"
	static final String PRECONDITION_PACKAGE_PROPERTY = "lbdsl.packages.pre"
	static final String CHANGE_PACKAGE_PROPERTY = "lbdsl.packages.change"
	static final String DB_DRIVER_PROPERTY = "lbdsl.db.driverClass"
	static final String DB_USER_PROPERTY = "lbdsl.db.username"
	static final String DB_PASS_PROPERTY = "lbdsl.db.password"
	static final String DB_URL_PROPERTY = "lbdsl.db.url"

	private static Properties cache = null; 

	static LbdslProperties getInstance() {
		return new LbdslProperties();
	}

	String getDefaultAuthor() {
		return System.getProperty('user.name') ?: "NOT_SUPPLIED"
	}

  void addChangePackage(String pkg) {
    if(!this.properties[CHANGE_PACKAGE_PROPERTY]) {
      this.properties[CHANGE_PACKAGE_PROPERTY] = pkg
    } else {
      this.properties[CHANGE_PACKAGE_PROPERTY] = ",$pkg"
    }
  }

  List getChangePackages() {
		def out = (this.properties[CHANGE_PACKAGE_PROPERTY]?.split(",") ?: []) as List
		out << "liquibase.change"
		out << "liquibase.change.custom"
		return out
  }

	List getPreconditionPackages() {
		def out = (this.properties[PRECONDITION_PACKAGE_PROPERTY]?.split(",") ?: []) as List
		out << "liquibase.preconditions"
		return out
	}

	/**
	*  Provides a list of strings of packages to search for commands within.  Result is never null.
	*/
	List getCommandPackages() {
		return this.properties[COMMAND_PACKAGE_PROPERTY]?.split(",") ?: [] as List
	}

	Connection getConnection() {
		// Modification of code swiped from liquibase Grails plugin
		// TODO Move this into Liquibase core: specify driverClassName, username, password, url; get Database
    Properties p = this.properties;
    String driverClassName = p[DB_DRIVER_PROPERTY]
    String username = p[DB_USER_PROPERTY]
    String password = p[DB_PASS_PROPERTY]
    String url = p[DB_URL_PROPERTY]


		Driver driver;
		try {
				if (driverClassName) {
					driver = (Driver) Class.forName(driverClassName).newInstance()
				} else {
						driver = DatabaseFactory.getInstance().findDefaultDriver(url)
						if (driver == null) {
							throw new RuntimeException("Driver class was not specified and could not be determined from the url")
						}
				}
				if(!driver) {
					throw new RuntimeException("Could not load driver");
				}
		} catch (Exception e) {
				throw new RuntimeException("Cannot get database driver: " + e.getMessage())
		}
		Properties info = new Properties()
		info.put("user", username)
		if (password != null) {
				info.put("password", password);
		}
		def conn = driver.connect(url, info); 
		if(!conn) {
			throw new IllegalArgumentException("Driver '${driver.class}' does not claim to handle connection string '${url}'")
		} else {
			return conn
		}
	}

	Database getDatabase() {
		def conn = this.connection
		if(!conn) {
			throw new IllegalStateException("Need to be able to get a connection to determine the database")
		}
		if(!conn.metaData) {
			throw new IllegalStateException("Cannot retrieve metadata from connection; necessary to determine the database")
		}
		return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn)
	}

	/**
	*	Provides a map of suffix to parser class name for the plugin parsers.  Result is never null.
	*/
	Map getPluginParsers() {
		return (this.properties.findAll { it.key.startsWith(SUFFIX_PARSER_PROPERTY) }?.inject([:]) { memo, entry ->
			def suffix = entry.key.substring(SUFFIX_PARSER_PROPERTY.size())
			def clsName = entry.value
			memo[suffix] = clsName	
			memo
		} ?: [:])
	}

	Properties getProperties() {
			if(!cache) {
				cache = fetchCache();
			}
			return cache;
	}

	private static Properties fetchCache() {
		final Properties props = System.getProperties();
		def homeDir = props.get("lbdsl.home");
		if(!homeDir) {
			throw new IllegalStateException("Property \"lbdsl.home\" is not set.");
		}
		homeDir = new File(homeDir)
    if(!homeDir.exists()) {
      homeDir.mkdirs()
    } else if(!homeDir.isDirectory()) {
			throw new IllegalStateException("Property \"lbdsl.home\" does not denote a directory (was $homeDir)")
		}

		homeDir.eachFileRecurse { f ->
			try {
				def name = f.getAbsolutePath()
				if(name.endsWith(".properties")) {
					f.withInputStream { is ->
						props.load(is)
					}
				} else if(name.endsWith(".groovy")) {
					props.addAll((Map)Eval.me(f.text))
				} else if(name.endsWith(".xml")) {
					f.withInputStream { is ->
						props.loadFromXML(is)
					}
				}
			} catch(Exception e) {
				throw new RuntimeException("Could not get the properties out of $f: ${e.getMessage()}", e)
			}
		}	

		return props
	}

}



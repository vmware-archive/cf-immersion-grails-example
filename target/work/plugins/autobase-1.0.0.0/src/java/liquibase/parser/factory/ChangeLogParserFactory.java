package liquibase.parser.factory;
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

import liquibase.parser.ChangeLogParserImpl;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* A thread-safe factory which returns a new ChangeLogParser instance for a given suffix.
*/
public class ChangeLogParserFactory {

	/**
	*	The thread-safe map of sufficies to ChangeLogParser classes.
	*/
	private static final Map<String,Class<? extends ChangeLogParserImpl>> SUFFIX_MAP = new ConcurrentHashMap<String, Class<? extends ChangeLogParserImpl>>(3, .75f, 2);

	/**
	* Registers a particular suffix with the factory.  The ChangeLogParser must provide a no-argument constructor and extend from the ChangeLogParser class.
	*/
	public static void register(String suffix, Class<? extends ChangeLogParserImpl> parserClass) {
		if(suffix == null) { throw new IllegalArgumentException("Suffix (argument 1) may not be null"); }
		if(parserClass == null) { throw new IllegalArgumentException("Parser class (argument 2) may not be null"); }
		newInstance(parserClass); // Make sure we can get an instance
		SUFFIX_MAP.put(suffix, parserClass);
	}

	public static void register(String suffix, String parserClassName) {
		if(parserClassName == null) { throw new IllegalArgumentException("Parser class name (argument 2) may not be null"); }
		try {
			final Class<?> cls = Class.forName(parserClassName);
			if(!ChangeLogParserImpl.class.isAssignableFrom(cls)) {
				throw new RuntimeException("Class " + cls.toString() + " does not inherit from " + ChangeLogParserImpl.class.toString());
			}
			final Class<? extends ChangeLogParserImpl> parserCls = (Class<? extends ChangeLogParserImpl>)cls; // Not a checked cast (type erasure)
			register(suffix, parserCls);
		} catch(Exception e) {
			throw new IllegalArgumentException("Class name \"" + parserClassName + "\" will not work as a parser.", e);
		}
	}

	/**
	* Provides a parser for the given suffix, if one is found.  If the exact suffix itself is not found, attempts to find a close match.  If multiple matches are possible,
	*	which particular match is used is undefined.
	*/
	public static ChangeLogParserImpl getParser(String suffix) {
		Logger log = LogFactory.getLogger();
		if(suffix == null) { throw new IllegalArgumentException("Suffix (argument 1) may not be null"); }
		Class<? extends ChangeLogParserImpl> cls = SUFFIX_MAP.get(suffix);
		if(cls == null) {
			log.info("Could not find a precise match for suffix \"" + suffix + "\"");
			for(final Map.Entry<String, Class<? extends ChangeLogParserImpl>> me : SUFFIX_MAP.entrySet()) {
				if(suffix.endsWith(me.getKey())) { cls = me.getValue(); }
			}
		}
		if(cls == null) {
			log.warning("Could not find any match for suffix \"" + suffix + "\"");
			return null;
		} else {
			ChangeLogParserImpl out = newInstance(cls);
			return out;
		} 
	}

	private static ChangeLogParserImpl newInstance(Class<? extends ChangeLogParserImpl> cls) {
		try {
			return cls.newInstance();
		} catch(Exception e) {
			throw new RuntimeException("Could not instantiate \"" + cls.toString() + "\"", e);
		}
	}

}

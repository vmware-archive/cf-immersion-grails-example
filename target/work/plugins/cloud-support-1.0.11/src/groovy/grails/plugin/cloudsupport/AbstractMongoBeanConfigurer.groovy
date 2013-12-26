/* Copyright 2011 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.cloudsupport

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * The Mongo update is done in the plugin descriptor's doWithSpring, so it's split
 * out into this class instead of in the bean postprocessor.
 *
 * @author Burt Beckwith
 */
abstract class AbstractMongoBeanConfigurer {

	protected Logger log = Logger.getLogger(getClass())

	void fixMongo(GrailsApplication application) {

		def updatedValues = findMongoValues(application)
		if (!updatedValues) {
			log.debug 'Not updating Mongo'
			return
		}

		def conf = application.config.grails.mongo
		conf.databaseName = updatedValues.databaseName
		conf.host = updatedValues.host
		conf.port = updatedValues.port
		conf.password = updatedValues.password
		conf.username = updatedValues.userName

		log.debug "Updated Mongo from $updatedValues"
	}

	/**
	 * Return updated Mongo connect info. Return an empty or null map to indicate that
	 * no processing should be done. Values should include:
	 * 	databaseName
	 * 	host
	 * 	port
	 * 	userName
	 * 	password
	 * @param application the application
	 * @return the data
	 */
	protected abstract Map findMongoValues(GrailsApplication application)
}

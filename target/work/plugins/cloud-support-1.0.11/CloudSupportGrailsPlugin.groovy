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

import grails.util.Metadata

class CloudSupportGrailsPlugin {
	String version = '1.0.11'
	String grailsVersion = '1.3.3 > *'
	String author = 'Burt Beckwith'
	String authorEmail = 'beckwithb@vmware.com'
	String title = 'Cloud Support Plugin'
	String description = 'Cloud Support Plugin'
	String documentation = 'http://grails.org/plugin/cloud-support'
	List pluginExcludes = [
		'docs/**',
		'src/docs/**'
	]
	def loadBefore = Metadata.current.getGrailsVersion().startsWith('1') ? [] : ['rabbitmq']

	String license = 'APACHE'
	def organization = [name: 'SpringSource', url: 'http://www.springsource.org/']
	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPCLOUDSUPPORT']
	def scm = [url: 'https://github.com/grails-plugins/grails-cloud-support']

	def doWithSpring = {
		// set dummy values so the plugin configures itself; the bean post-processor will set the real values.
		// this only works in 2.0+ since there's a bug in loadBefore handling if the plugin isn't installed
		def config = application.config.rabbitmq.connectionfactory
		if (!config) {
			config.hostname = 'placeholder'
			config.username = 'placeholder'
			config.password = 'placeholder'
		}
	}

	def doWithApplicationContext = { ctx ->
		if (ctx.containsBean('redisDatastore')) {
			ctx.redisDatastore.applicationContext = ctx
		}
	}
}

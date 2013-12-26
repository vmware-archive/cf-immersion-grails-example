package autobase

import liquibase.*
import liquibase.resource.ResourceAccessor;

import org.codehaus.groovy.grails.commons.ApplicationHolder

class GrailsResourceAccessorFactory {

	static ResourceAccessor getResourceAccessor() {
		if (ApplicationHolder.application.isWarDeployed()) {
    	return new GrailsClassLoaderResourceAccessor()
    } else {
    	return new GrailsFileSystemResourceAccessor()
    }		
	}

	
}


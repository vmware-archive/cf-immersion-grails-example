	includeTargets << grailsScript("_GrailsInit")
	includeTargets << grailsScript("_GrailsCreateArtifacts")
	Ant.mkdir(dir:"${basedir}/grails-app/migrations")
	target('default': "Creates a new configuration migration file ") {
	    depends(checkVersion, parseArguments)
	    def type = "Migration"
	    promptForName(type: type)
	    def name = argsMap["params"][0]
		createArtifact(name: name, suffix: type, type: type, path: "grails-app/migrations")	
	}
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.apache.commons.lang.StringUtils as SU
import groovy.xml.QName

includeTargets << grailsScript("Init")
includeTargets << new File("${autobasePluginDir}/scripts/_MigrationBase.groovy")

target(default: "Converts a full or partial changelog.xml file into a changelog.groovy file") {
  args = args?.split(/\s+/)
  String sourceName = args && args.size() > 1 ? args.getAt(0) : null
  String targetName = args && args.size() > 2 ? args.getAt(1) : null

  if(!sourceName) {
    ant.input(addProperty:"autobase.convert.source", message: "What file should we convert?")
    sourceName = ant.antProject.properties."autobase.convert.source"
  }
  if(sourceName == null || SU.isBlank(sourceName)) {
    ant.fail(message:"No source file to convert specified")
  }
 
  File sourceFile = new File(sourceName)
  if(!sourceFile.exists()) {
    ant.fail(message:"Source file to convert does not exist: ${sourceFile.absolutePath}")
  }
  if(!sourceFile.isFile()) {
    ant.fail(message:"Source file to convert is not a file: ${sourceFile.absolutePath}")
  }
  if(!sourceFile.canRead()) {
    ant.fail(message:"Cannot read source file: ${sourceFile.absolutePath}")
  }

  if(!targetName) {
    targetName = sourceName
  }

  targetName = SU.removeEnd(sourceName, ".xml") + ".groovy"
  event("StatusUpdate", ["Converting ${sourceFile.path} to a Groovy migration"])

  sourceFile.withInputStream { inputStream ->
    xmlToGroovyMigration(inputStream, targetName)
  }
}

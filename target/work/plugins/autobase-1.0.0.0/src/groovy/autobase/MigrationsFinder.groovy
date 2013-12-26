package autobase;

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH;
import java.util.zip.*

class MigrationsFinder {

  static def findMigrations() {
    def changeLogs
    if(AH.application.isWarDeployed()) {
      def resources = new ZipInputStream(AH.application.classLoader.getResourceAsStream("Autobase.Migrations.zip"))
      changeLogs = [:]
      def entry
      while(entry = resources.nextEntry()) {
        if(!entry.isDirectory())  {
          def outLine = new ByteArrayOutputStream()
          while(resources.available()) {
            outLine.write(resources.read())
          }
          outLine.close()
          resources.closeEntry()
          changeLogs[entry.name] = new ByteArrayInputStream(outLine.toByteArray())
        }
      }
      changeLogs = (changeLogs.entrySet() as List).sort({a,b -> a.key <=> b.key })*.values
    } else {
      changeLogs = changeLogFilesFromDir(new File("./app-grails/migrations")) 
    }
    return changeLogs
  }  

  private static final def changeLogFilesFromDir(final File dir) {
    def changeLogs = []
    def root = dir.listFiles()
    root.findAll { it.isDirectory() }.each { changeLogs << changeLogsFilesFromDir(it) }
    changeLogs << (root.findAll {
      it.isFile() && it.name.endsWith('Migration.groovy')
    } ?: [])*.canonicalPath
    return changeLogs.flatten()*.sort( {a,b -> a.name <=> b.name })
  }

}

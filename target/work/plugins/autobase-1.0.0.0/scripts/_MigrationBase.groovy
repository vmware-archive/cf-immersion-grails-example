import groovy.xml.QName
import java.text.SimpleDateFormat
import liquibase.dsl.properties.LbdslProperties as Props
import org.apache.commons.lang.StringUtils as SU
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

includeTargets << grailsScript("Init")

target(findAuthor:"Finds the author and sets the appropriate property") {
  def author = Props.instance.defaultAuthor
  author = author.split(/\s+/).collect { SU.capitalize(it) }.join("")
  ant.property(name:'migration.author',value:author)
}

target(makeMigrationDir: "Creates the directory for migrations") {
  depends(findAuthor)
  def author = ant.antProject.properties.'migration.author'
  def dir = "./migrations/${author}"
  ant.property(name:'migration.dir',value:dir)
	ant.mkdir(dir:dir)
}

target(makeMigration:'Creates the migration script itself') {
  depends(findAuthor)
  depends(makeMigrationDir)
  def name = ant.antProject.properties.'migration.name' ?: args?.toString()
  if(!name) {
    ant.input(addProperty:"migration.name.orig", message:"Migration name not specified (press enter for auto-generated name)")
    name = ant.antProject.properties."migration.name.orig"
  }
  if(name == null || SU.isBlank(name)) {
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
    name = timestamp
  }
  name = name.split(/\s+/).collect { SU.capitalize(it) }.join("")
  ant.property(name:"migration.name", value:name)

  def dir = ant.antProject.properties.'migration.dir'
  def author = ant.antProject.properties.'migration.author'

  def fileName = "${dir}/${name}Migration.groovy"
  ant.property(name:'migration.file', value:fileName)

  def file = ant.antProject.properties.'migration.template' ?: "${autobasePluginDir}/src/templates/artifacts/Migration.groovy"

  ant.sequential {
    copy(file:file, tofile:fileName) 
    replace(file:fileName, token:"@AUTHOR@", value:author )
    replace(file:fileName, token:"@ID@", value:name )
  }
  println "Migration generated at ${fileName}"
}

xmlToGroovyMigration = { inputStream, targetName ->
  def targetFile = new File("./migrations", targetName)
  event("StatusUpdate", ["Generating migration script ${targetFile.path}"])
  targetFile.withWriter { writer ->
    new XmlParser().parse(inputStream).children().each { processNode(it, writer) }
  }
}

private String stripNamespace(final String str) {
  if(str.contains('}')) {
    return SU.substringAfterLast(str, '}')
  } else {
    return str
  }
}

private String stripNamespace(final QName nodeName) {
  return nodeName.localPart
}

private void processNode(final Node node, final Writer writer, final int depth=0) {
  final String nodeName = stripNamespace(node.name())

  //println "Processing node ${nodeName}"
  depth.times { writer.write('\t') }; writer.write(nodeName)
  writer.write('( ')
  List inParens = []
  String textBody = node.children().find { it instanceof String }
  if(textBody) { 
    textBody = SU.replace(textBody, '\n', '\\n')
    inParens << "\"${textBody}\""
  }
  def attrs = node.attributes().entrySet()
  if(nodeName == 'databaseChangeLog') { attrs = attrs.findAll { stripNamespace(it.key) != 'schemaLocation' } }
  attrs.each { inParens << "${stripNamespace(it.key)}: \"$it.value\"" }
  writer.write(inParens.join(', ') ?: '')
  writer.write(' )')
  def children = node.children().findAll { !(it instanceof String) }
  if(children) {
    (0..<depth).each { writer.write('\t') }; writer.write('{\n')
    children.each { 
      processNode(it, writer, depth + 1)
    }
    (0..<depth).each { writer.write('\t') }; writer.write('}\n')
  }
  writer.write('\n')
}

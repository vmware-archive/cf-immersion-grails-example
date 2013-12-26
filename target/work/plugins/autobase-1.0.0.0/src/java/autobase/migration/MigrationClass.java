package autobase.migration;
import groovy.lang.Closure;
import org.codehaus.groovy.grails.commons.GrailsClass;
public interface MigrationClass extends GrailsClass{

    public void setMigration();
    public Closure getMigration();
    
}

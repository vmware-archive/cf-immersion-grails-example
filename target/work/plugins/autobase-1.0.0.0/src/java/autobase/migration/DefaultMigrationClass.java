package autobase.migration;

import groovy.lang.Closure;
import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;

public class DefaultMigrationClass extends AbstractGrailsClass implements
        MigrationClass {
    Closure migration;
    
    private Class originalClass;

    public DefaultMigrationClass(Class clazz) {
        super(clazz, MigrationArtefactHandler.SUFFIX);
        this.originalClass = clazz;
        setMigration();
    }

    public void setMigration() {
        // TODO Auto-generated method stub
        migration = (Closure) GrailsClassUtils.getStaticPropertyValue(
                getClazz(), "migration");
    }

    public Closure getMigration() {
        return migration;
    }

    public Class getOriginalClass() {
        return originalClass;
    }

    public void setOriginalClass(Class originalClass) {
        this.originalClass = originalClass;
    }

}

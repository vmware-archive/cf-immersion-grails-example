package liquibase.dsl.parser.groovy

import liquibase.change.*
import org.apache.commons.lang.StringUtils 
import liquibase.dsl.properties.*

class ColumnCatcher extends groovy.util.Proxy {

  ColumnCatcher(toWrap) {
    if(!toWrap) { throw new IllegalArgumentException("Needs a change to wrap") }
    adaptee = toWrap
  }

  // It'd be nice to eliminate this, but proxy of a proxy doesn't work
  // http://jira.codehaus.org/browse/GROOVY-3345
  void where(String s) {
    adaptee.whereClause = s
  }

  void column(Map m=[:], Closure c={->}) {
    Collection changePkgs = LbdslProperties.instance.getChangePackages()
    if(!changePkgs) {
      throw new IllegalStateException("No change packages found")
    }
    def classNames = [ adaptee.class.simpleName  ] as SortedSet
    classNames << StringUtils.removeEnd(adaptee.class.simpleName, "Change")
    classNames << StringUtils.removeEnd(adaptee.class.simpleName, "DataChange")
    classNames << StringUtils.removeStart(StringUtils.removeEnd(adaptee.class.simpleName, "DataChange"), "Raw")
    Class configClass = changePkgs.inject(null) { memo, pkg ->
      if(memo) {
        return memo
      }
      return classNames.inject(memo) { mem, className ->
        if(mem) { return mem }
        try {
          return Class.forName(pkg + "." + className + "ColumnConfig")
        } catch(ClassNotFoundException cnfe) {
          return mem
        }
      }
    } ?: ColumnConfig
    ColumnConfig col = configClass.newInstance()
    m.each { 
      try {
        if(it.key == "defaultValueBoolean") {
          col.setDefaultValueBoolean(it.value ? new Boolean(it.value) : null)
        } else if(it.key == "index") {
          col.index = it.value ? new Integer(it.value) : null
        } else {
            try {
                col."set${StringUtils.capitalize(it.key)}"(it.value?.toString())
            } catch (Throwable e) {
                col."set${StringUtils.capitalize(it.key)}"(it.value)
            }
        }
      } catch(Exception e) {
        throw new RuntimeException("Could not assign ${it.key} value ${it.value} (${it.value?.class})", e)
      }
    }
    c.delegate = new ConstraintsCatcher(col)
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    try {
      adaptee.addColumn(col)
    } catch(Exception e) {
      throw new IllegalStateException("Could not add ${col} to ${adaptee}: ${col.dump()}", e)
    }
  }

}

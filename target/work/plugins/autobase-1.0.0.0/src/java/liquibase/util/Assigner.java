package liquibase.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.time.DateUtils;
import java.util.Date;
import java.util.Map;

public class Assigner {

  static {
    ConvertUtils.register(new Converter() {
      public Object convert(final Class cls, final Object o) {
        if(o == null) {
          throw new IllegalArgumentException("Object to convert is null");
        }
        return new Integer(o.toString());
      }
    }, Number.class);
    ConvertUtils.register(new Converter() {
      public Object convert(final Class cls, final Object o) {
        if(o == null) {
          throw new IllegalArgumentException("Object to convert is null");
        }
        try {
          return DateUtils.parseDate(o.toString(), new String[] { 
            "yyyy-MM-dd", "HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyMMddHHmmssZ", 
            "yyyy-MM-dd'T'HH:mm:ssZ", "yyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"
          });
        } catch(java.text.ParseException pe) {
          throw new RuntimeException("Do not know how to convert " + o.toString(), pe);
        }
      }
    }, Date.class);
  }

  public static void assign(final Object bean, final Map props) {
    try {
      BeanUtils.populate(bean, props);
    } catch(RuntimeException re) {
      throw re;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

}

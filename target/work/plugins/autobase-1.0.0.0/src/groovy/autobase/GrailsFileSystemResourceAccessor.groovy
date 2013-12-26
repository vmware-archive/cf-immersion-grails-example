package autobase;

import liquibase.resource.ResourceAccessor;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class GrailsFileSystemResourceAccessor implements ResourceAccessor {
    public InputStream getResourceAsStream(String file) throws IOException {
        return getClass().getClassLoader().getResourceAsStream("grails-app/migrations/"+file);
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return getClass().getClassLoader().getResources("grails-app/migrations/"+packageName);
    }

    public ClassLoader toClassLoader() {
        return getClass().getClassLoader();
    }
}

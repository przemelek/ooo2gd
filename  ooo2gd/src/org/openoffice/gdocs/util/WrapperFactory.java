package org.openoffice.gdocs.util;

import java.util.HashMap;
import java.util.Map;

public class WrapperFactory {
    public static final String GOOGLE_DOCS = "Google Docs";
    public static final String WEBDAV = "WebDAV";
    public static final String ZOHO = "Zoho";
    
    private static Map<String,Wrapper> systems;
    
    public static Wrapper getWrapperForCredentials(String system) {
        if (systems==null) {
            systems = new HashMap<String, Wrapper>();
        }
        Wrapper wrapper = systems.get(system);
        if (wrapper==null) {
            if (GOOGLE_DOCS.equals(system)) {
                wrapper = new GoogleDocsWrapper();
            } else if (ZOHO.equals(system)) {
                wrapper = new ZohoWrapper();
            } else if (WEBDAV.equals(system)) {
                wrapper = new WebDAVWrapper();
            }
            systems.put(system, wrapper);
        }
        return wrapper;
    }
}

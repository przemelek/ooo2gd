package org.openoffice.gdocs.util;

import java.util.HashMap;
import java.util.Map;

public class WrapperFactory {
    
    private static Map<String,Wrapper> systems;
    
    public static Wrapper getWrapperForCredentials(String system) {
        if (systems==null) {
            systems = new HashMap<String, Wrapper>();
        }
        Wrapper wrapper = systems.get(system);
        if (wrapper==null) {
            if ("Google Docs".equals(system)) {
                wrapper = new GoogleDocsWrapper();
            } else if ("Zoho".equals(system)) {
                wrapper = new ZohoWrapper();
            } else if ("WebDAV".equals(system)) {
                wrapper = new WebDAVWrapper();
            }
            systems.put(system, wrapper);
        }
        return wrapper;
    }
}

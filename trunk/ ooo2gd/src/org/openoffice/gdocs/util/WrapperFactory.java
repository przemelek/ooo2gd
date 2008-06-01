package org.openoffice.gdocs.util;

public class WrapperFactory {
    public static Wrapper getWrapperForCredentials(String system) {        
        Wrapper wrapper = null;
        if ("Google Docs".equals(system)) {
            wrapper = new GoogleDocsWrapper();
        } else if ("Zoho".equals(system)) {
            wrapper = new ZohoWrapper();
        }
        return wrapper;
    }
}

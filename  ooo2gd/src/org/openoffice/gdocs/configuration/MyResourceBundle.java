package org.openoffice.gdocs.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

class MyResourceBundle extends ResourceBundle {

    private static Map<String,MyResourceBundle> cache = new HashMap<String,MyResourceBundle>();
    private Properties props;
    
    public static ResourceBundle getMyBundle(String path, Locale locale) {
        MyResourceBundle rb = null;
        List<String> candidates = new ArrayList<String>();
        candidates.add(path+"_"+locale.getLanguage()+"_"+locale.getCountry()+".properties");
        candidates.add(path+"_"+locale.getLanguage()+".properties");
        for (String candidate:candidates) {
            rb = cache.get(candidate);
            if (rb==null) {
                File f = new File(candidate);
                if (f.exists()) {
                    Properties p = new Properties();
                    try {
                        FileInputStream fis = new FileInputStream(f);
                        p.load(fis);
                        fis.close();
                        rb = new MyResourceBundle();
                        rb.props = p;
                        cache.put(candidate, rb);
                    } catch (Exception e) {
                        // we will ignore it, having null in rb tells us a much more than exception ;-)
                    }
                }
            }
        }
        return rb;
    }
    
    @Override
    protected Object handleGetObject(String key) {
        return props.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        Vector<String> keys = new Vector<String>();        
        for (Object obj:props.keySet()) {
            keys.add(obj.toString());
        }
        return keys.elements();
    }

}

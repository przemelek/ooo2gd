package org.openoffice.gdocs.configuration;

import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataListener;

public class Configuration {
    
    private static Map<String,String> map = new HashMap<String,String>();
    private static Map<String,String> langsMap = new HashMap<String,String>();
    
    private static String lang = "system";
    
    static {
        // OK, it's realy ugly method...        
        map.put("English","en");
        map.put("Polski","pl");
        map.put("German","de");
        map.put("System","system");
        for (String str : map.keySet()) {
            langsMap.put(map.get(str),str);
        }                
        restore();
    }
    
    /** Creates a new instance of Configuration */
    public Configuration() {

    }

    public void storeConfig() {
        //com.sun.star.registry.RegistryKeyType                        
        // get my global service manager  

//    XMultiServiceFactory xServiceManager = (XMultiServiceFactory)UnoRuntime.queryInterface( 
//            XMultiServiceFactory.class, this.getRemoteServiceManager("uno:socket,host=localhost,port=2083;urp;StarOffice.ServiceManager")); 

//    final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";

    // create the provider and remember it as a XMultiServiceFactory 

//    XMultiServiceFactory xProvider = (XMultiServiceFactory) 
//        UnoRuntime.queryInterface(XMultiServiceFactory.class, 
//                                  xServiceManager.createInstance(sProviderService));        
    }
    
    public static void store() {


        try {
            FileWriter fw = new FileWriter("gdocs.lang");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(lang);
            bw.close();
        } catch (Exception e) {
            
        }
    }
    
    public static void restore() {
        try {
            FileReader fr = new FileReader("gdocs.lang");
            BufferedReader br = new BufferedReader(fr);
            lang = br.readLine();
            br.close();
            
        } catch (Exception e) {
            
        }
    }
    
    public static ResourceBundle getResources() {        
        Locale locale = Locale.getDefault();
        if ((lang!=null) && (!"system".equals(lang))) locale = new Locale(lang);
        return ResourceBundle.getBundle("org/openoffice/gdocs/resources/properties",locale);
    }
    
    public static ComboBoxModel getLanguagesModel() {
        ComboBoxModel model = new DefaultComboBoxModel(map.keySet().toArray());
        model.setSelectedItem(langsMap.get(lang));
        return model;
    }
    
    public static void setLang(String chosenLang) {
        lang = map.get(chosenLang);
        store();
    }
}

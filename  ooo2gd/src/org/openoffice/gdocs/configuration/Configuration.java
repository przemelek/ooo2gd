package org.openoffice.gdocs.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import java.util.Properties;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.openoffice.gdocs.util.EncodingSensitiveControl;
import org.openoffice.gdocs.util.OOoUtil;

public class Configuration {
        
    private static boolean useProxy;
    private static boolean proxyAuth;
    private static String proxyServer;
    private static String proxyPort;
    private static String proxyUser;
    private static String proxyPassword;
    private static Map<String,String> map = new LinkedHashMap<String, String>();
    private static Map<String,String> langsMap = new HashMap<String,String>();
    private static final String CONFIG_SECRET_PHRASE = "p@cpo(#";
    private static String lang = "system";
    
    static {
        // OK, it's realy ugly method...        
        map.put("English","en");
        map.put("Bulgarian", "bg");
        map.put("German","de");
        map.put("Polski","pl");                
        map.put("Russian","ru");
        map.put("System","system");
        for (Entry<String, String> entry : map.entrySet()) {
            langsMap.put(entry.getValue(),entry.getKey());
        }                
        restore();
    }
    
    /** Creates a new instance of Configuration */
    public Configuration() {

    }

//    public void storeConfig() {
        //com.sun.star.registry.RegistryKeyType                        
        // get my global service manager  

//    XMultiServiceFactory xServiceManager = (XMultiServiceFactory)UnoRuntime.queryInterface( 
//            XMultiServiceFactory.class, this.getRemoteServiceManager("uno:socket,host=localhost,port=2083;urp;StarOffice.ServiceManager")); 

//    final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";

    // create the provider and remember it as a XMultiServiceFactory 

//    XMultiServiceFactory xProvider = (XMultiServiceFactory) 
//        UnoRuntime.queryInterface(XMultiServiceFactory.class, 
//                                  xServiceManager.createInstance(sProviderService));        
//    }
    
    public static void store() {
    	BufferedWriter bw = null;
    	PrintWriter pr = null;
        try {
            FileWriter fw = new FileWriter(getWorkingPath()+"gdocs.lang");
            bw = new BufferedWriter(fw);
            pr = new PrintWriter(bw);
        	pr.println(lang);
        	pr.println(isUseProxy()?"1":"0");
        	pr.println(getProxyServer());
        	pr.println(getProxyPort());
        	pr.println(isProxyAuth()?"1":"0");
        	pr.println(OOoUtil.xorString(getProxyUser(),CONFIG_SECRET_PHRASE));
        	pr.println(OOoUtil.xorString(getProxyPassword(),CONFIG_SECRET_PHRASE));
        } catch (Exception e) {
            // Intentionaly left empty
        } finally {
        	if (pr!=null) {
        			pr.close();
        	}
        }
        restore();
    }
    
    public static void restore() {
        try {
            FileReader fr = new FileReader(getWorkingPath()+"gdocs.lang");
            BufferedReader br = new BufferedReader(fr);
            lang = br.readLine();
            String useProxyStr = br.readLine();
            String proxyServer = br.readLine();
            String proxyPort = br.readLine();
            setUseProxy("1".equals(useProxyStr));
            setProxyServer(proxyServer);
            setProxyPort(proxyPort);
            String proxyAuthStr = br.readLine();
            setProxyAuth("1".equals(proxyAuthStr));
            String proxyUser = br.readLine();
            String proxyPassword = br.readLine();
            if ((proxyUser!=null) && (proxyPassword!=null)) {
            	setProxyUser(OOoUtil.xorString(proxyUser, CONFIG_SECRET_PHRASE));
            	setProxyPassword(OOoUtil.xorString(proxyPassword, CONFIG_SECRET_PHRASE));
            }
            br.close();
            
        } catch (IOException e) {
            // Intentionaly left empty
        }
        setProxyProperties(isUseProxy(), isProxyAuth());
    }

	private static void setProxyProperties(boolean isUseProxy,
			boolean isProxyAuth) {
		if (isUseProxy) { 
            setProxyAuthenticator(isProxyAuth);            
            Properties systemProperties = System.getProperties();
//            systemProperties.setProperty("http.proxySet", "true");
//            systemProperties.setProperty("https.proxySet", "true");
            systemProperties.setProperty("http.proxyHost", getProxyServer());
            systemProperties.setProperty("http.proxyPort", getProxyPort());
            systemProperties.setProperty("https.proxyHost", getProxyServer());
            systemProperties.setProperty("https.proxyPort", getProxyPort());            
        } else {
            Authenticator.setDefault(null);
            Properties systemProperties = System.getProperties();            
            systemProperties.remove("http.proxySet");
            systemProperties.remove("https.proxySet");            
            systemProperties.remove("http.proxyHost");
            systemProperties.remove("http.proxyPort");
            systemProperties.remove("https.proxyHost");
            systemProperties.remove("https.proxyPort");            
        }
	}

	private static void setProxyAuthenticator(boolean isProxyAuth) {
		if (isProxyAuth) {
		    class SimpleAuthenticator extends Authenticator {
		        protected PasswordAuthentication getPasswordAuthentication() {
		            return new PasswordAuthentication(Configuration.getProxyUser(), Configuration.getProxyPassword().toCharArray());
		        }                
		    }
		    Authenticator.setDefault(new SimpleAuthenticator());
		} else {
		    Authenticator.setDefault(null);
		}
	}
    
    private static final EncodingSensitiveControl encodingSensitiveControl = new EncodingSensitiveControl();
    public static ResourceBundle getResources() {        
        Locale locale = Locale.getDefault();
        if ((lang!=null) && (!"system".equals(lang))) locale = new Locale(lang);
        return ResourceBundle.getBundle("org/openoffice/gdocs/resources/properties", 
            locale, 
            encodingSensitiveControl);
    }
    
    public static ComboBoxModel getLanguagesModel() {
        ComboBoxModel model = new DefaultComboBoxModel(map.keySet().toArray());
        model.setSelectedItem(langsMap.get(lang));
        return model;
    }
    
    public static void setLang(String chosenLang) {
        lang = map.get(chosenLang);
    }
    
    public static String getWorkingPath() {
        return System.getProperty("user.home")+"/";
    }

        public static String getProxyPassword() {
        return proxyPassword;
    }

    public static void setProxyPassword(String proxyPassword) {
        Configuration.proxyPassword = proxyPassword;
    }

    public static String getProxyPort() {
        return proxyPort;
    }

    public static void setProxyPort(String proxyPort) {
        Configuration.proxyPort = proxyPort;
    }

    public static String getProxyServer() {
        return proxyServer;
    }

    public static void setProxyServer(String proxyServer) {
        Configuration.proxyServer = proxyServer;
    }

    public static String getProxyUser() {
        return proxyUser;
    }

    public static void setProxyUser(String proxyUser) {
        Configuration.proxyUser = proxyUser;
    }

    public static boolean isUseProxy() {
        return useProxy;
    }

    public static void setUseProxy(boolean userProxy) {
        Configuration.useProxy = userProxy;
    }
    
    public static boolean isProxyAuth() {
        return proxyAuth;
    }
    
    public static void setProxyAuth(boolean proxyAuth) {
        Configuration.proxyAuth=proxyAuth;
    }
    
}

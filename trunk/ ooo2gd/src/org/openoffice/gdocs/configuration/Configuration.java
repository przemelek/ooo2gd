package org.openoffice.gdocs.configuration;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.File;
import java.io.FilenameFilter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.openoffice.gdocs.ui.dialogs.WaitWindow;
import org.openoffice.gdocs.util.OOoFormats;
import org.openoffice.gdocs.util.Util;
import org.openoffice.gdocs.util.WrapperFactory;
import org.openoffice.gdocs.util.Wrapper;

public class Configuration {
    private static class FileInfo {
        private String fName;
        private String documentLink;
        private long length;
        private long lastModified;
        private OOoFormats format;
        public FileInfo(String fName,String documentLink,OOoFormats format) {
            setfName(fName);
            setDocumentLink(documentLink);
            setFormat(format);
        }
        String getfName() {
            return fName;
        }
        void setfName(String fName) {
            this.fName = fName;
        }
        long getLength() {
            return length;
        }
        void setLength(long length) {
            this.length = length;
        }
        long getLastModified() {
            return lastModified;
        }
        void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
        String getDocumentLink() {
            return documentLink;
        }
        void setDocumentLink(String docID) {
            this.documentLink = docID;
        }
        OOoFormats getFormat() {
            return format;
        }
        void setFormat(OOoFormats format) {
            this.format = format;
        }
        @Override
        public int hashCode() {
            return fName.hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FileInfo) {
                FileInfo fi2 = (FileInfo)obj;
                return this.getfName().equals(fi2.getfName());
            } else return false;
        }
    }

    private static final int MAX_SIZE_OF_LOG = 1000;    
    private static final String CONFIG_SECRET_PHRASE = "p@cpo(#";
    private static String versionStr = "2.0.0";
    private static List<String> log = new ArrayList<String>();
    private static boolean useProxy;
    private static boolean proxyAuth;
    private static String proxyServer;
    private static String proxyPort;
    private static String proxyUser;
    private static String proxyPassword;
    private static Map<String,String> map = new LinkedHashMap<String, String>();
    private static Map<String,String> langsMap = new HashMap<String,String>();    
    private static String lang = "system";
    // null means store in work dir, ? means ask, otherwise we have here path to directory.
    private static String directoryToStoreFiles;
    private static ClassLoader classLoader;    
    private static WaitWindow waitWindow = null;
    private static boolean useExec = false;
    private static String pathForOOoExec;
    private static String pathForBrowserExec;
    private static boolean overwritteFlag;
    private static String lookAndFeel;
    private static boolean defaultAutoUpdate;
    private static Timer syncTimer;

//    static private Map<String,String> globalMapOfFiles = new HashMap<String, String>();
    static private Set<FileInfo> setOfFiles = new HashSet<FileInfo>();
    
    static {
        // OK, it's ugly method...        
        map.put("English","en");
        map.put("Bulgarian", "bg");
        map.put("German","de");
        map.put("Polski","pl");
        map.put("Russian","ru");
        map.put("Spanish","es");
        map.put("Italian","it");
        map.put("Portuguese (Brazilian)","pt_br");
        map.put("Chinese (Simplified)","zh_cn");
        map.put("French","fr");
        map.put("System","system");
        for (Entry<String, String> entry : map.entrySet()) {
            langsMap.put(entry.getValue(),entry.getKey());
        }                
        restore();
    }

    public static void changeLookAndFeel(String lookAndFeel) {       
        if (!UIManager.getLookAndFeel().getName().equals(lookAndFeel)) {
            for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
                if (lafInfo.getName().equals(lookAndFeel)) {
                    try {
                        UIManager.setLookAndFeel(lafInfo.getClassName());
                    } catch (Exception e) {
                        log(e);
                    }
                    break;
                }
            }
        }
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
            pr.println(Util.xorString(getProxyUser(),CONFIG_SECRET_PHRASE));
            pr.println(Util.xorString(getProxyPassword(),CONFIG_SECRET_PHRASE));
            String directoryToStoreFiles = getDirectoryToStoreFiles();
            if (directoryToStoreFiles==null) directoryToStoreFiles="";
            pr.println(directoryToStoreFiles);
            pr.println(isUseExec()?"1":"0");
            pr.println(pathForBrowserExec);
            pr.println(pathForOOoExec);
            pr.println(getOverwritteFlag()?"1":"0");
            pr.println(lookAndFeel);
            pr.println(isDefaultAutoUpdate()?"1":"0");
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
        BufferedReader br = null;
        try {
            lookAndFeel=UIManager.getLookAndFeel().getName();
            FileReader fr = new FileReader(getWorkingPath()+"gdocs.lang");
            br = new BufferedReader(fr);
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
            	setProxyUser(Util.xorString(proxyUser, CONFIG_SECRET_PHRASE));
            	setProxyPassword(Util.xorString(proxyPassword, CONFIG_SECRET_PHRASE));
            }
            String directoryToStoreFiles = br.readLine();
            if ("".equals(directoryToStoreFiles)) directoryToStoreFiles = null;
            setDirectoryToStoreFiles(directoryToStoreFiles);
            String useExecStr = br.readLine();
            setUseExec("1".equals(useExecStr));
            String pathForBrowserExec = br.readLine();
            setPathForBrowserExec(pathForBrowserExec);
            String pathForOOoExec = br.readLine();
            setPathForOOoExec(pathForOOoExec);
            String overwritteFlagStr = br.readLine();
            setOverwritteFlag("1".equals(overwritteFlagStr));
            String newLookAndFeel = br.readLine();
            if (newLookAndFeel!=null) {
                lookAndFeel=newLookAndFeel;
                changeLookAndFeel(lookAndFeel);
            }
            String defaultAutoupdateString = br.readLine();
            setDefaultAutoUpdate("1".equals(defaultAutoupdateString));
        } catch (IOException e) {
            // Intentionaly left empty
        } finally {
            if (br!=null) {
                try {
                    br.close();
                } catch (IOException e) { }
            }
        }
        setProxyProperties(isUseProxy(), isProxyAuth());
    }

    private static String getPathToExec(Component parent, String dialogText) throws HeadlessException {
        String pathToExec = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogText);
        if (fileChooser.showDialog(parent, "Choose") == JFileChooser.APPROVE_OPTION) {
            pathToExec = fileChooser.getSelectedFile().getAbsolutePath();
        }
        return pathToExec;
    }

    private static String returnFirstExistingFile(List<String> paths) {
        String retPath = null;
        for (String path : paths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                retPath = path;
                break;
            }
        }
        return retPath;
    }

        private static void setProxyProperties(boolean isUseProxy, boolean isProxyAuth) {
            if (isUseProxy) { 
                setProxyAuthenticator(isProxyAuth);            
                Properties systemProperties = System.getProperties();
                systemProperties.setProperty("http.proxyHost", getProxyServer());
                systemProperties.setProperty("http.proxyPort", getProxyPort());
                systemProperties.setProperty("https.proxyHost", getProxyServer());
                systemProperties.setProperty("https.proxyPort", getProxyPort());            
            } else {
                Authenticator.setDefault(null);
                Properties systemProperties = System.getProperties(); 
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
        
    public static ResourceBundle getResources() {
        Locale locale = Locale.getDefault();
        if ((lang!=null) && (!"system".equals(lang))) locale = new Locale(lang);
        ResourceBundle rb = null;        
        try {            
            rb = MyResourceBundle.getMyBundle(getWorkingPath()+"ooo2gd",locale);
        } catch (Exception e) {
            e.printStackTrace();;
        }
        if (rb==null) {
            rb = ResourceBundle.getBundle("org/openoffice/gdocs/resources/ooo2gd", locale);
        }
        return rb;
    }
    
    public static String getStringFromResources(String stringId) {
        return Configuration.getResources().getString(stringId);
    }
    
    public static String getStringFromResources(String stringId,String system) {
        return getStringFromResources(stringId).replace("${system}", system);
    }
    
    public static String getStringFromResources(String stringId,String... args) {
          String str = getStringFromResources(stringId);
	  int idx = 0;
	  for (String value:args) {
		  str = str.replaceAll("\\$\\{"+(idx++)+"\\}", value);
	  }
	  str=str.replaceAll("\\$\\{[^\\}]\\}", "?");
	  return str;        
    }
    
    public static ComboBoxModel getLanguagesModel() {        
        File f = new File(getWorkingPath());
        final Pattern p = Pattern.compile("ooo2gd\\_(\\w{2}(\\_\\w{2}){0,1})\\.properties");
        String[] fNames = f.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {                
                boolean result = p.matcher(name).matches();
                return result;
            }
        });
        for (String fName:fNames) {
            Matcher m = p.matcher(fName);
            m.find();
            String langCode = m.group(1);
            String langName = new Locale(langCode).getDisplayLanguage();
            if (!map.containsKey(langName)) {
                map.put(langName,langCode);
            }
        }
//        langsMap.clear();
        for (Entry<String, String> entry : map.entrySet()) {
            langsMap.put(entry.getValue(),entry.getKey());
        }                        
        ComboBoxModel model = new DefaultComboBoxModel(map.keySet().toArray());
        model.setSelectedItem(langsMap.get(lang));
        return model;
    }
    
    public static ComboBoxModel getLooksAndFeelsModel() {
        List<String> list = new ArrayList<String>();
        for (UIManager.LookAndFeelInfo lafInfo:UIManager.getInstalledLookAndFeels()) {
            list.add(lafInfo.getName());
        }
        ComboBoxModel model = new DefaultComboBoxModel(list.toArray());
        if (lookAndFeel!=null && list.contains(lookAndFeel)) {
            model.setSelectedItem(lookAndFeel);
        }
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

    public static void log(String str) {
        if (log.size()>MAX_SIZE_OF_LOG) {
            log.remove(0);
        }
        log.add(str);        
    }
    
    public static void log(Exception e) {        
        class MyWriter extends Writer {
            private StringBuilder sb;

            public MyWriter() {
                sb = new StringBuilder();
            }

            @Override
            public void close() throws IOException {
                
            }

            @Override
            public void flush() throws IOException {
                
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                sb.append(cbuf, off, len);
            }

            @Override
            public String toString() {
                return sb.toString();
            }                        
        };
        MyWriter writer = new MyWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        log(writer.toString());            
    }
    
    public static List<String> getLog() {
        return log;
    }
    
    public static String getVersionStr() {
        return versionStr;
    }
    
    public static void setClassLoader(ClassLoader cl) {
        classLoader = cl;
    }
    
    public static ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public static void showWaitWindow() {
        getWaitWindow();
        waitWindow.setVisible(true);
    }
    
    public static void hideWaitWindow() {
        getWaitWindow();
        waitWindow.setVisible(false);
    }

    public static Component getWaitWindow() {
        if (waitWindow==null) {
            waitWindow = new WaitWindow();
        }        
        return waitWindow;
    }
    
    public static String getDirectoryToStoreFiles() {
        return directoryToStoreFiles;
    }

    public static void setDirectoryToStoreFiles(String aDirectoryToStoreFiles) {
        directoryToStoreFiles = aDirectoryToStoreFiles;
    }
    
    public static String getPathToDirectoryToStorefiles() {
        String directoryToStoreFiles = getDirectoryToStoreFiles();
        if (directoryToStoreFiles==null) directoryToStoreFiles = getWorkingPath();
        return directoryToStoreFiles;
    }

    public static boolean isUseExec() {
        if (Util.isMac()) {
            return true;
        }
        return useExec;
    }
    
    public static void setUseExec(boolean useExecVal) {
        useExec = useExecVal;
    }

    public static String getPathForOOoExec(Component parent) {
        if (pathForOOoExec==null || "null".equals(pathForOOoExec)) {
            if (Util.isMac()) {
                List<String> paths = new ArrayList<String>();
                paths.add("/Applications/OpenOffice.org.app");
                paths.add("/Applications/OpenOffice.org.app/Contents/MacOS/soffice");
                pathForOOoExec = returnFirstExistingFile(paths);
            }
            if (pathForOOoExec==null || "null".equals(pathForOOoExec)) {
                JOptionPane.showMessageDialog(parent, "OOo2GD needs to know where is your OpenOffice.org executable file, please select executable file of your OpenOffice.org.");
                pathForOOoExec = getPathToExec(parent, "Choose your OpenOffice.org executable");
            }                
        }
        return pathForOOoExec;
    }

    public static void setPathForOOoExec(String pathForOOoExecVal) {
        pathForOOoExec = pathForOOoExecVal;
    }
    
    public static String getPathForBrowserExec(Component parent) {
        if (pathForBrowserExec==null || "null".equals(pathForBrowserExec)) {
            if (Util.isMac()) {
                List<String> paths = new ArrayList<String>();
                paths.add("/Applications/Safari.app/Contents/MacOS/Safari");
                paths.add("/Applications/Safari.app");
                paths.add("/Applications/Firefox.app ");
                pathForBrowserExec = returnFirstExistingFile(paths);                
            }
            if (pathForBrowserExec==null || "null".equals(pathForBrowserExec)) {
                JOptionPane.showMessageDialog(parent, "OOo2GD needs to know where is your browser executable file, please select executable file of your browser.");
                pathForBrowserExec = getPathToExec(parent,"Choose your browser executable");            
            }
        }        
        return pathForBrowserExec;
    }
    
    public static void setPathForBrowserExec(String pathForBrowserExecVal) {
        pathForBrowserExec = pathForBrowserExecVal;
    }

    public static void setOverwritteFlag(boolean flag) {
        overwritteFlag = flag;
    }
    
    public static boolean getOverwritteFlag() {
        return overwritteFlag;
    }
    
    public static void setLookAndFeel(String lookAndFeel) {
        Configuration.lookAndFeel = lookAndFeel;
    }
    
    public static String getLookAndFeel() {
        return Configuration.lookAndFeel;
    }

    public static boolean isDefaultAutoUpdate() {
        return defaultAutoUpdate;
    }

    public static void setDefaultAutoUpdate(boolean aDefaultAutoUpdate) {
        defaultAutoUpdate = aDefaultAutoUpdate;
    }

    public static void addToGlobalMapOfFiles(String documentUrl,String docID,OOoFormats format) {
        FileInfo fi = new FileInfo(documentUrl,docID,format);
        boolean addFileToList = true;
        for (FileInfo fi2:setOfFiles) {
            if (fi.getDocumentLink().equals(fi2.getDocumentLink()) && !fi.equals(fi2)) {
                addFileToList = false;
                String str = "fi.fName="+fi.getfName()+" fi2.fName="+fi2.getfName()+"\nfi.docLink="+fi.getDocumentLink()+"\nfi2.docLink="+fi2.getDocumentLink();
                log.add(str);
                JOptionPane.showMessageDialog(null, "Problem!!\nFiles "+documentUrl+" and "+fi2.getfName()+" points to the same element, OOo2GD will synchronize only "+fi2.getfName());
                break;
            }
        }
        if (addFileToList) {
            setOfFiles.add(fi);
        }
    }

    private static synchronized void startSyncTimerIfNeeded() {
        if (syncTimer==null) {
            syncTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Set<FileInfo> setToWork = Collections.synchronizedSet(setOfFiles);
                    Wrapper wrapper = WrapperFactory.getWrapperForCredentials(WrapperFactory.GOOGLE_DOCS);
                    boolean needToUpdateList = false;
                    for (FileInfo fi:setToWork) {
                        File file = new File(fi.getfName());
                        boolean update = false;
                        if (fi.getLastModified()!=file.lastModified()) update = true;
                        if (fi.getLength()!=file.length()) update = true;
                        if (update) {
                            log.add("need to update file "+fi.getfName()+" with docID="+fi.getDocumentLink()+" with mimeType="+fi.getFormat().getMimeType());
                            needToUpdateList=true;
                            try {
                                wrapper.update(fi.getfName(),fi.getDocumentLink(),fi.getFormat().getMimeType());
                                fi.setLastModified(file.lastModified());
                                fi.setLength(file.length());
                                log.add("updated!");
                            } catch (Exception e) {
                                log(e);
                            }

                        }
                    }
                    if (needToUpdateList) {
                        try {
                            wrapper.getListOfDocs(false);
                        } catch (Exception ex) {
                            log(ex);
                        }
                    }
                }
            };
            // 3 minutes
            syncTimer.scheduleAtFixedRate(task, 0, 1*60*100);
        }
    }

    public static void modifyGlobalMapOfFiles(String documnetUrl,long length,long lastModified) {
        for (FileInfo fi:setOfFiles) {
            if (fi.getfName()==documnetUrl) {
                fi.setLastModified(lastModified);
                fi.setLength(length);
                startSyncTimerIfNeeded();
            }
        }
    }
}

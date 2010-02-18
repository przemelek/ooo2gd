package org.openoffice.gdocs.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import org.openoffice.gdocs.configuration.Configuration;

public class WebDAVWrapper implements Wrapper {

    private static class WebDAVCredentialsManager {
        private static final String CREDITIONALS_FILE = "gdocs.datWebDAVServers";
        private static final String SECRET_PHRASE = "$ogorek#";
   
        private static Map<String,Creditionals> map;
        
        private static void readCredetials() {
            String credentionalsFileName = Configuration.getConfigFileName(Configuration.getWorkingPath(),CREDITIONALS_FILE);
            map = new HashMap<String, Creditionals>();
            try {            
                File file = new File(credentionalsFileName);
                if (file.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(credentionalsFileName));
                    StringBuilder sb = new StringBuilder();
                    int length;
                    char[] buf = new char[1024];
                    while ((length=br.read(buf))!=-1) {                                                
//                        sb.append(Arrays.copyOf(buf, length));
                        sb.append(new String(buf, 0, length));
                    }
                    String decoded = Util.xorString(sb.toString(), SECRET_PHRASE);
                    decoded+="....";
                    String[] lines = decoded.split("\n");
                    for (int idx=0; idx<lines.length/3; idx++) {
                        String serverPath = lines[idx*3];
                        String userName = lines[idx*3+1];
                        String userPassword = lines[idx*3+2];
                        map.put(serverPath,new Creditionals(userName, userPassword, "WebDAV"));
                    }                    
                }
            } catch (IOException e) {                
            }
        }
        
        private static void store() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String,Creditionals> entry:map.entrySet()) {
                String serverPath = entry.getKey();
                if (entry.getValue()==null) continue;
                String userName = entry.getValue().getUserName();
                String userPassword = entry.getValue().getPassword();                
                sb.append(serverPath).append("\n");
                sb.append(userName).append("\n");
                sb.append(userPassword).append("\n");
            }
            sb.append(".");
            try {
                String coded = Util.xorString(sb.toString(), SECRET_PHRASE);
                String credentionalsFileName = Configuration.getConfigFileName(Configuration.getWorkingPath(),CREDITIONALS_FILE);
                FileWriter fw = new FileWriter(credentionalsFileName);
                fw.write(coded.toCharArray());
                fw.close();
            } catch (Exception e) {
                // OK, it's probably bad that we wasn't able to store credentials.....     
                // but we will ignore this, it's easier ;-)
            }
        }
        
        public static Creditionals getCredentialsForServer(String serverPath) {
            if (map==null) readCredetials();
            Creditionals creditionals = map.get(serverPath);
            if (creditionals==null) {
                creditionals = new Creditionals("", "", "WebDAV");
            }
            return creditionals;
        }
        
        public static List<String> getServersList() {
            readCredetials();            
            List<String> list = new LinkedList<String>();
            list.addAll(map.keySet());
            return list;
        }

        private static void storeCredentialsFor(String serverPath, Creditionals credentials) {
            if (map==null) readCredetials();
            map.put(serverPath, credentials);
            store();
        }
    }
    
    private String serverPath = "";    
    
    public Downloader getDownloader(URI uri, String documentUrl) throws URISyntaxException, MalformedURLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI getUriForEntry(Document entry) throws URISyntaxException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI getUriForEntryInBrowser(Document entry) throws URISyntaxException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void login(Creditionals creditionals) throws Exception {
        final String userName = creditionals.getUserName();
        final String userPassword = creditionals.getPassword();
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                PasswordAuthentication pa = new PasswordAuthentication(userName, userPassword.toCharArray());
                return pa;
            }
            
        });
    }
    
    public boolean upload(String path, String documentTitle,String mimeType) throws Exception {
            FileInputStream fis = new FileInputStream(path);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
            long contentLength = getStream(fis,baos);
            byte[] fileContent =  baos.toByteArray();
            int responseCode = upload(documentTitle, contentLength, fileContent);
            System.out.println(responseCode);
            return ((responseCode>=200) && (responseCode<300));
    }

    public boolean checkIfAuthorizationNeeded(String path,String documentTitle) throws Exception {
        byte[] buf = new byte[1];
        int responseCode = upload(documentTitle,buf.length,buf);
        return (responseCode==401);
    }
        
        private int getStream(final InputStream is, OutputStream out) throws IOException {
        int progress = 0;
        byte[] buffer = new byte[1024*8];            
        int readCount;        
        while((readCount=is.read(buffer))>0) {
            String bufStr = new String(buffer);
          out.write(buffer, 0, readCount);
          progress += readCount;
        }
        out.flush();
        out.close();
        out = null;
        return progress;
    }
    
    public boolean neededConversion(String path) {
        return false;
    }

    public boolean neededConversion(OOoFormats format) {
        return false;
    }
    
    public OOoFormats convertTo(OOoFormats format) {
        return format;
    }
    
    public String closestSupportedFormat(String path) {
        return path.substring(path.lastIndexOf("."));
    }

    public String getSystem() {
        return "WebDAV";
    }

    public List<Document> getListOfDocs(boolean useCachedListIfPossible) throws Exception {
        // PROPFIND
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private String getServerPath() {
        return serverPath;
    }
    
    public void setServerPath(String serverPath) {
        if (!serverPath.endsWith("/")) {
            serverPath+="/";
        }
        this.serverPath=serverPath;
    }
    
    public boolean isServerSelectionNeeded() {
        return true;
    }

    public List<String> getListOfServersForSelection() {
        return WebDAVCredentialsManager.getServersList();        
    }

    public Creditionals getCreditionalsForServer(String serverPath) {
        return WebDAVCredentialsManager.getCredentialsForServer(serverPath);
    }

    public void storeCredentials(Creditionals credentials) {
            WebDAVCredentialsManager.storeCredentialsFor(serverPath,credentials);
    }
    
    
    
    private int upload(String documentTitle, long contentLength, byte[] fileContent) throws MalformedURLException, IOException, IOException, ProtocolException {
        String uploadUri = getServerPath() + documentTitle.replace(" ", "%20");
        System.out.println(uploadUri);
        URL source = new URL(uploadUri);

        HttpURLConnection.setDefaultAllowUserInteraction(true);

        HttpURLConnection conn = (HttpURLConnection) source.openConnection();
        conn.setRequestMethod("PUT");

        conn.setRequestProperty("Content-Length", "" + contentLength);
        conn.setDoOutput(true);
        conn.connect();
        conn.getOutputStream().write(fileContent);
        conn.getOutputStream().close();
        int responseCode = conn.getResponseCode();
        return responseCode;
    }

    public boolean updateSupported() {
        return false;
    }

    public boolean update(String path, String docId, String mimeType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean downloadInGivenFormatSupported() {
        return false;
    }

    public List<OOoFormats> getListOfSupportedForDownloadFormatsForEntry(Document entry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI getUriForEntry(Document entry, final OOoFormats format) throws URISyntaxException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Date parseDate(String date) throws ParseException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
//    public static void main(String[] args) throws Exception {
//        Wrapper wrapper = new WebDAVWrapper();
//        ((WebDAVWrapper)(wrapper)).setServerPath("http://localhost:8080/webdavek/");
//        Authenticator.setDefault(new Authenticator() {
//
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                PasswordAuthentication pa = new PasswordAuthentication("", "".toCharArray());
//                return pa;
//            }
//            
//        });
//        wrapper.login(new Creditionals("a", "a"));
//        System.out.println(((WebDAVWrapper)wrapper).checkIfAuthorizationNeeded("e:\\test 001.ppt", "test 001.ppt"));
//        boolean b = wrapper.upload("e:\\test 001.ppt", "test 001.ppt");
//        System.out.println(b);
//    }
    
}

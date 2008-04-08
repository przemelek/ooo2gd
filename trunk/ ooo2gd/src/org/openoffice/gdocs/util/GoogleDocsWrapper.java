// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.util;

import com.google.gdata.util.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.util.AuthenticationException;

public class GoogleDocsWrapper {	
	public static final String APP_NAME = "RMK OpenOffice.org Docs Uploader";
	public static final String DOCS_FEED = "http://docs.google.com/feeds/documents/private/full";
	private DocsService service;
        private boolean isLogedIn = false;
	
        public GoogleDocsWrapper() {
        }
        
        public DocsService getService() {
            return service;
        }
        
	public void login(Creditionals creditionals) throws AuthenticationException {
            if (!isLogedIn) {
		service = new DocsService(APP_NAME);
		service.setUserCredentials(creditionals.getUserName(),creditionals.getPassword());
                isLogedIn=true;
            } else {
                System.out.println("LogedIn :-)");
                Throwable t = new Throwable();
                t.printStackTrace();
            }
	}
	
	public boolean upload(String path,String documentTitle) throws IOException, ServiceException {
              boolean result = false;
              File documentFile = getFileForPath(path);               
              uploadFile(documentFile, documentTitle);
              result=true;
              return result;
	}

         private File getFileForPath(final String path) throws FileNotFoundException, IOException {
        
            File documentFile = new File(path);
        
            if (path.split("\\.").length>2) {
                String ext = path.substring(path.lastIndexOf("."));                
                String name = path.substring(path.lastIndexOf("\\")+1,path.lastIndexOf("."));
                name = name.replaceAll("\\.","_");
                File tmpFile = File.createTempFile(name,ext);
                tmpFile.deleteOnExit();          
                InputStream in = new FileInputStream(documentFile);
                OutputStream out = new FileOutputStream(tmpFile);
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                documentFile = tmpFile;
            } 
            return documentFile;
        }

    private String getGoogleAppsName(final Creditionals credetionals) {
        String googleApsName = "";
        if (credetionals != null) {
            String userName = credetionals.getUserName();
            if (userName.indexOf("@") != -1) {
                googleApsName = "a/" + userName.substring(userName.indexOf("@") + 1) + "/";
            }
        }
        return googleApsName;
    }

        private void uploadFile(final File documentFile, final String documentTitle) throws IOException, MalformedURLException, ServiceException {
            DocumentEntry newDocument = new DocumentEntry();
              newDocument.setFile(documentFile);
              newDocument.setTitle(new PlainTextConstruct(documentTitle));
              URL documentListFeedUrl = new URL(DOCS_FEED);
              DocumentListEntry uploaded = service.insert(documentListFeedUrl, 
                  newDocument);
	}
	
	public List<DocumentListEntry> getListOfDocs() throws IOException, ServiceException {
		List<DocumentListEntry> list = new LinkedList<DocumentListEntry>();
                URL documentFeedUrl = new URL(DOCS_FEED); 
                DocumentListFeed feed = service.getFeed(documentFeedUrl,DocumentListFeed.class);
                list=feed.getEntries();                
		return list;
        }
        
        public URI getUriForEntry(final DocumentListEntry entry,final Creditionals credetionals) throws URISyntaxException {
            String googleApsName = getGoogleAppsName(credetionals);
            String id = entry.getId().split("%3A")[1];
            String type = entry.getId().split("%3A")[0];            
            String uriStr = "";
            if ("document".equals(type)) {
                uriStr = "https://docs.google.com/"+googleApsName+"MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=oo";
            } else if ("spreadsheet".equals(type)) {
                //uriStr = "http://spreadsheets.google.com/fm?id="+id+"&hl=en&fmcmd=13";
                uriStr = "https://spreadsheets.google.com/"+googleApsName+"ccc?key="+id+"&hl=en";
            } else if ("presentation".equals(type)) {
                uriStr = "https://docs.google.com/"+googleApsName+"MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=ppt";
            }
            return new URI(uriStr);
        }	
	
        public URI getUriForEntryInBrowser(final DocumentListEntry entry,final Creditionals credetionals) throws URISyntaxException {
            String googleApsName = getGoogleAppsName(credetionals);
            String id = entry.getId().split("%3A")[1];
            String type = entry.getId().split("%3A")[0];            
            String uriStr = "";
            if ("document".equals(type)) {
                uriStr = "https://docs.google.com/"+googleApsName+"Doc?docid="+id;
            } else if ("spreadsheet".equals(type)) {
                //uriStr = "http://spreadsheets.google.com/fm?id="+id+"&hl=en&fmcmd=13";
                uriStr = "https://spreadsheets.google.com/"+googleApsName+"ccc?key="+id+"&hl=en";
            } else if ("presentation".equals(type)) {
                uriStr = "https://docs.google.com/"+googleApsName+"MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=ppt";
            }
            return new URI(uriStr);            
        }
}

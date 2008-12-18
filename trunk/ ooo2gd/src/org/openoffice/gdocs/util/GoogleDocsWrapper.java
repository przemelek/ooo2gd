// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.util;

import com.google.gdata.client.GoogleService;
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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.ui.dialogs.CaptchaDialog;

public class GoogleDocsWrapper implements Wrapper {	
	public static final String APP_NAME = "RMK OpenOffice.org Docs Uploader";
	public static final String DOCS_FEED = "http://docs.google.com/feeds/documents/private/full";
	private DocsService service;
        private boolean isLogedIn = false;
        private static List<Document> listOfDocuments;
        private static Map<Document,DocumentListEntry> doc2Entry = null;
        
        public GoogleDocsWrapper() {
        }
        
        public DocsService getService() {
            return service;
        }
        
	public void login(Creditionals creditionals) throws AuthenticationException {            
            if (!isLogedIn) {
                Configuration.log("Try to create DocsService");
		service = new DocsService(APP_NAME);
                Configuration.log("DocsService created");
                try {
                    Configuration.log("Try to login");
                    service.setUserCredentials(creditionals.getUserName(),creditionals.getPassword());
                    isLogedIn=true;
                    Configuration.log("LogedIn");
                } catch (GoogleService.CaptchaRequiredException captchaException) {
                    Configuration.log("Problem with login");
                    Configuration.log(captchaException);
                    CaptchaDialog dialog = new CaptchaDialog(captchaException.getCaptchaUrl());
                    dialog.setModal(true);
                    dialog.setVisible(true);
                    if (dialog.getReturnCode()==JOptionPane.OK_OPTION) {
                        service.setUserCredentials(creditionals.getUserName(),creditionals.getPassword(),captchaException.getCaptchaToken(),dialog.getReturnValue());
                        isLogedIn=true;
                    }
                }                
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

        public boolean checkIfAuthorizationNeeded(String path, String documentTitle) throws Exception {
            return true;
        }

        public void storeCredentials(Creditionals credentials) {
            credentials.store();
        }

        public void setServerPath(String serverPath) {
            // do nothing
        }

        public boolean isServerSelectionNeeded() {
            return false;
        }

        public List<String> getListOfServersForSelection() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Creditionals getCreditionalsForServer(String serverPath) {
            throw new UnsupportedOperationException("Not supported yet.");
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

        private void uploadFile(final File documentFile, final String documentTitle) throws IOException, MalformedURLException, ServiceException {
              DocumentEntry newDocument = new DocumentEntry();
              newDocument.setFile(documentFile);
              newDocument.setTitle(new PlainTextConstruct(documentTitle));
              URL documentListFeedUrl = new URL(DOCS_FEED);
              DocumentListEntry uploaded = service.insert(documentListFeedUrl, 
                  newDocument);
	}
	
        public List<Document> getListOfDocs(boolean useCachedListIfPossible) throws IOException, ServiceException {
            if (!useCachedListIfPossible || listOfDocuments==null) {
                Configuration.log("Try to get list of docs...");                
		List<Document> listOfDocuments = new LinkedList<Document>();
                doc2Entry = new HashMap<Document, DocumentListEntry>();
                URL documentFeedUrl = new URL(DOCS_FEED); 
                DocumentListFeed feed = service.getFeed(documentFeedUrl,DocumentListFeed.class);
                List<DocumentListEntry> listOfEntries = feed.getEntries();
                for (DocumentListEntry entry:listOfEntries) {
                    Document docEntry = new Document();
                    docEntry.setDocumentLink(entry.getDocumentLink().getHref());
                    docEntry.setId(entry.getId());
                    docEntry.setTitle(entry.getTitle().getPlainText());
                    docEntry.setUpdated(entry.getUpdated().toStringRfc822());
                    doc2Entry.put(docEntry, entry);
                    listOfDocuments.add(docEntry);
                }
                Configuration.log("List has "+listOfDocuments.size()+" elements.");
                this.listOfDocuments=listOfDocuments;
            }
            return this.listOfDocuments;
        }
        
        public URI getUriForEntry(final Document entry) throws URISyntaxException {
            String id = entry.getId().split("%3A")[1];
            String type = entry.getId().split("%3A")[0];
            String entryLink = entry.getDocumentLink();
            String uriStr = entryLink.substring(0,entryLink.lastIndexOf("/")+1).replace("http:","https:");
            if ("document".equals(type)) {
                uriStr += "MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=oo";
            } else if ("spreadsheet".equals(type)) {
                //uriStr = "http://spreadsheets.google.com/fm?id="+id+"&hl=en&fmcmd=13";
                //uriStr = "https://docs.google.com/"+googleApsName+"MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=ods";
                uriStr += "ccc?key="+id+"&hl=en";
            } else if ("presentation".equals(type)) {
                //http://docs.google.com/MiscCommands?command=saveasdoc&exportFormat=ppt&docID=ajg23wkfz9qn_449gt2kn8
                uriStr += "MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=ppt";
            }
            return new URI(uriStr);
        }	
	
        public URI getUriForEntryInBrowser(final Document entry) throws URISyntaxException {
            String uriStr = "";
            uriStr = entry.getDocumentLink();
            return new URI(uriStr);            
        }

        public boolean neeedConversion(String path) {
            if (path.toLowerCase().endsWith(".odp")) {
                return true;
            }
            return false;
        }

        public String closestSupportedFormat(String path) {
            String extension = path.substring(path.lastIndexOf(".")+1).toLowerCase();
            if ("odp".equals(extension)) {
                return "ppt";
            } else {
                return extension;
            }
        }

        public String getSystem() {
            return "Google Docs";
        }

        public Downloader getDownloader(URI uri, String documentUrl) throws URISyntaxException, MalformedURLException  {
            return new Downloader(uri, documentUrl, this);
        }

        @Override
        public boolean updateSupported() {
            return true;
        }

        @Override
        public boolean update(String path, String docId)  throws Exception {
            List<Document> docs = getListOfDocs(true);
            Map<String,Document> mapOfDocs = new HashMap<String,Document>();
            for (Document doc:docs) {
                mapOfDocs.put(doc.getDocumentLink(),doc);
            }
            Document docToUpdate = mapOfDocs.get(docId);
            DocumentListEntry entry = doc2Entry.get(docToUpdate);
            entry.setFile(getFileForPath(path));
            service.updateMedia(new URL(entry.getEditLink().getHref()), entry);
            return true;
        }
        
        
        
}

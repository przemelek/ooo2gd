// (c) 2007-2009 by Przemyslaw Rumik
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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.http.HttpAuthToken;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.util.AuthenticationException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.ui.dialogs.CaptchaDialog;

public class GoogleDocsWrapper implements Wrapper {	
        public static final OOoFormats[] SUPPORTED_FORMATS = {OOoFormats.Text,OOoFormats.HTML_Document_OpenOfficeorg_Writer,OOoFormats.OpenDocument_Text,
                                                              OOoFormats.OpenOfficeorg_10_Text_Document,OOoFormats.Microsoft_Word_97_2000_XP,
                                                              OOoFormats.Microsoft_Word_95,OOoFormats.Microsoft_Word_60,OOoFormats.Rich_Text_Format,
                                                              OOoFormats.Microsoft_PowerPoint_97_2000_XP,OOoFormats.Microsoft_Excel_97_2000_XP,
                                                              OOoFormats.Microsoft_Excel_95,OOoFormats.Microsoft_Excel_50,OOoFormats.OpenDocument_Spreadsheet,
                                                              OOoFormats.Text_CSV};
        // Yo Google! Sad that you didn't publish statistics for 3rd party programs using Google Docs API
        // btw. Cracow office looks nice from inside ;-)
	public static final String APP_NAME = "RMK-OpenOffice.orgDocsUploader-"+Configuration.getVersionStr();
	public static final String DOCS_FEED = "http://docs.google.com/feeds/documents/private/full";        
	private DocsService service;
        private SpreadsheetService spreadsheetService;
        private Creditionals creditionals;
        private boolean isLogedIn = false;
        private static List<Document> listOfDocuments;
        private static Map<Document,DocumentListEntry> doc2Entry = null;
        private DateFormat parseDf;
        
        public GoogleDocsWrapper() {
            parseDf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        }
        
        public DocsService getService() {
            return service;
        }
        
        public SpreadsheetService getSpreadsheetService() {
            return spreadsheetService;
        }
        
	public void login(Creditionals creditionals) throws AuthenticationException {
            if (!creditionals.equals(this.creditionals) || !isLogedIn) {
                Configuration.log("Try to create DocsService");
		service = new DocsService(APP_NAME);
                spreadsheetService = new SpreadsheetService(APP_NAME);
                Configuration.log("DocsService created");
                try {
                    Configuration.log("Try to login");
                    service.setUserCredentials(creditionals.getUserName(),creditionals.getPassword());
                    spreadsheetService.setUserCredentials(creditionals.getUserName(),creditionals.getPassword());
                    isLogedIn=true;
                    this.creditionals = creditionals;
                    listOfDocuments = null;
                    Configuration.log("LogedIn");
                } catch (GoogleService.CaptchaRequiredException captchaException) {
                    Configuration.log("Problem with login");
                    Configuration.log(captchaException);
                    CaptchaDialog dialog = new CaptchaDialog(captchaException.getCaptchaUrl());
                    dialog.setModal(true);
                    dialog.setVisible(true);
                    if (dialog.getReturnCode()==JOptionPane.OK_OPTION) {
                        service.setUserCredentials(creditionals.getUserName(),creditionals.getPassword(),captchaException.getCaptchaToken(),dialog.getReturnValue());
                        spreadsheetService.setUserCredentials(creditionals.getUserName(),creditionals.getPassword(),captchaException.getCaptchaToken(),dialog.getReturnValue());
                        isLogedIn=true;
                        this.creditionals = creditionals;
                        listOfDocuments = null;                        
                    }
                }                
            } else {
                System.out.println("LogedIn :-)");
            }
	}
	
	public boolean upload(String path,String documentTitle,String mimeType) throws IOException, ServiceException {
              boolean result = false;
              File documentFile = getFileForPath(path);               
              uploadFile(documentFile, documentTitle,mimeType);
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

        private void uploadFile(final File documentFile, final String documentTitle, final String mimeType) throws IOException, MalformedURLException, ServiceException {
              DocumentEntry newDocument = new DocumentEntry();
              newDocument.setFile(documentFile,mimeType);
              newDocument.setTitle(new PlainTextConstruct(documentTitle));
              URL documentListFeedUrl = new URL(DOCS_FEED);
              DocumentListEntry uploaded = service.insert(documentListFeedUrl, 
                  newDocument);
	}
	
        public List<Document> getListOfDocs(boolean useCachedListIfPossible) throws IOException, ServiceException {
            if (!useCachedListIfPossible || listOfDocuments==null) {
                Configuration.log("Try to get list of docs...");                
		List<Document> listOfDocuments = new LinkedList<Document>();
                if (doc2Entry==null) {
                    Configuration.log("Create new doc2Entry");
                    doc2Entry = new HashMap<Document, DocumentListEntry>();    
                }
                doc2Entry.clear();
                URL documentFeedUrl = new URL(DOCS_FEED); 
                DocumentListFeed feed = service.getFeed(documentFeedUrl,DocumentListFeed.class);
                List<DocumentListEntry> listOfEntries = feed.getEntries();
                int i=0;
                for (DocumentListEntry entry:listOfEntries) {
                    Document docEntry = new Document();
                    docEntry.setDocumentLink(entry.getDocumentLink().getHref());
                    docEntry.setId(entry.getId());
                    docEntry.setTitle(entry.getTitle().getPlainText());
                    docEntry.setUpdated(entry.getUpdated().toStringRfc822());
//                    System.out.println(entry.getTitle().getPlainText());
//                    AclFeed aclFeed = service.getFeed(new URL(entry.getAclFeedLink().getHref()), AclFeed.class);
//                    for (AclEntry entryAcl : aclFeed.getEntries()) {
//                      System.out.println(
//                          entryAcl.getScope().getValue() + " (" + entryAcl.getScope().getType() + ") : " + entryAcl.getRole().getValue());
//                    }
//                    for (Person person:entry.getAuthors()) {
//                        System.out.println(person.getName()+" "+person.getEmail());
//                    }
                    if (isDoc(docEntry) || isSpreadsheet(docEntry) || isPresentation(docEntry)) {
                        doc2Entry.put(docEntry, entry);
                        listOfDocuments.add(docEntry);
                    }
                }
                Configuration.log("List has "+listOfDocuments.size()+" elements.");
                this.listOfDocuments=listOfDocuments;
            }
            return this.listOfDocuments;
        }
        
//       private int getStream(final InputStream is, OutputStream out) throws IOException {
//        int progress = 0;
//        byte[] buffer = new byte[1024*8];
//        int readCount;
//        while((readCount=is.read(buffer))>0) {
//          out.write(buffer, 0, readCount);
//          progress += readCount;
//        }
//        out.flush();
//        out.close();
//        out = null;
//        return progress;
//    }
        
       public URI getUriForEntry(final Document entry) throws URISyntaxException {
           OOoFormats defaultFormat = OOoFormats.OpenDocument_Text;
           if (isSpreadsheet(entry)) {
               defaultFormat = OOoFormats.OpenDocument_Spreadsheet;
           } else if (isPresentation(entry)) {
               defaultFormat = OOoFormats.OpenDocument_Presentation;
           }
           return getUriForEntry(entry,defaultFormat);
       }
       
        public URI getUriForEntry(final Document entry, final OOoFormats format) throws URISyntaxException {
            String id = entry.getId().split("%3A")[1];
            String type = entry.getId().split("%3A")[0];
            type = type.substring(type.lastIndexOf("/")+1);
            String entryLink = entry.getDocumentLink();
            String uriStr = entryLink.substring(0,entryLink.lastIndexOf("/")+1).replace("http:","https:");
            String formatStr;
            if ("document".equals(type)) {
                formatStr = format.getFileExtension();
                uriStr+="feeds/download/documents/Export?docID="+id+"&exportFormat="+formatStr;
            } else if ("spreadsheet".equals(type)) {
                switch (format) {
                    case OpenDocument_Spreadsheet: {
                        formatStr = "13";
                        break;  
                    }
                    case Microsoft_Excel_97_2000_XP: {
                        formatStr = "4";
                        break;
                    }
                    case Text_CSV: {
                        formatStr = "5";
                        break;
                    }
                    default: formatStr="13";
                }
                uriStr+= "feeds/download/spreadsheets/Export?key="+id+"&fmcmd="+formatStr;
            } else if ("presentation".equals(type)) {
                // not sure why, but it looks that Export servlet URL is now
                // http://docs.google.com/present/export?format=ppt&id=
                formatStr = format.getFileExtension();
//                uriStr+="feeds/download/presentations/Export?docID="+id+"&exportFormat="+formatStr;
                uriStr="https://docs.google.com/present/export?format="+formatStr+"&id="+id;
            }
            return new URI(uriStr);
        }	
	

        public URI getUriForEntryInBrowser(final Document entry) throws URISyntaxException {
            String uriStr = "";
            uriStr = entry.getDocumentLink();
            return new URI(uriStr);
        }

        public boolean neededConversion(String path) {
            if (path.toLowerCase().endsWith(".odp")) {
                return true;
            }
            return false;
        }
        
        public boolean neededConversion(OOoFormats format) {
            return !(java.util.Arrays.asList(SUPPORTED_FORMATS).contains(format));
        }

        public String closestSupportedFormat(String path) {
            String extension = path.substring(path.lastIndexOf(".")+1).toLowerCase();
            if ("odp".equals(extension)) {
                return "ppt";
            } else {
                return extension;
            }
        }
        
        public OOoFormats convertTo(OOoFormats format) {
            OOoFormats destinationFormat = format;
            if (neededConversion(format)) {
                if (format.getHandlerType()==0) {
                    // Text document
                    destinationFormat = OOoFormats.OpenDocument_Text;
                } else if (format.getHandlerType()==1) {
                    // Spreadsheet
                    destinationFormat = OOoFormats.OpenDocument_Spreadsheet;
                } if (format.getHandlerType()==2) {
                    // Presentations
                    destinationFormat = OOoFormats.Microsoft_PowerPoint_97_2000_XP;
                }
            }
            return destinationFormat;
        }

        public String getSystem() {
            return "Google Docs";
        }

        public Downloader getDownloader(URI uri, String documentUrl) throws URISyntaxException, MalformedURLException  {
            Downloader downloader = new Downloader(uri, documentUrl, this);
            String path = uri.getPath();
            GoogleService service;
            if (path.indexOf("/feeds/download/spreadsheets")!=-1) {
                service = getSpreadsheetService();
            } else {
                service = getService();
            }
            HttpAuthToken authToken = (HttpAuthToken)service.getAuthTokenFactory().getAuthToken();
            downloader.setAuthToken(authToken);
            return downloader;
        }

        public boolean updateSupported() {
            return true;
        }

        public boolean update(String path, String docId,String mimeType)  throws Exception {
            List<Document> docs = getListOfDocs(true);
//            Map<String,Document> mapOfDocs = new HashMap<String,Document>();
            Document docToUpdate = null;
            for (Document doc:docs) {
//                mapOfDocs.put(doc.getDocumentLink(),doc);
                if (doc.getDocumentLink().equals(docId)) {
                    docToUpdate = doc;
                    break;
                }
            }
//            Document docToUpdate = mapOfDocs.get(docId);
            if (docToUpdate!=null) {
                DocumentListEntry entry = doc2Entry.get(docToUpdate);
                entry.setFile(getFileForPath(path),mimeType);
                DocumentListEntry updatedEntry = service.updateMedia(new URL(entry.getEditLink().getHref()), entry);
                Configuration.log("entry==updatedEntry is "+(entry==updatedEntry));
                System.out.println("entry==updatedEntry is "+(entry==updatedEntry));
                doc2Entry.put(docToUpdate, updatedEntry);
                //getListOfDocs(false);
                return true;
            } else {
                Configuration.log(path+" will not be updated.");
                return false;
            }
        }

    public boolean downloadInGivenFormatSupported() {
        return true;
    }

    public List<OOoFormats> getListOfSupportedForDownloadFormatsForEntry(Document entry) {
        List<OOoFormats> formats = new ArrayList<OOoFormats>();
        if (isDoc(entry)) {
            formats.add(OOoFormats.OpenDocument_Text);
            formats.add(OOoFormats.Microsoft_Word_97_2000_XP);
            formats.add(OOoFormats.Rich_Text_Format);
            formats.add(OOoFormats.Text);
        } else if (isPresentation(entry)) {
            formats.add(OOoFormats.Microsoft_PowerPoint_97_2000_XP);
        } else if (isSpreadsheet(entry)) {
            formats.add(OOoFormats.OpenDocument_Spreadsheet);
            formats.add(OOoFormats.Microsoft_Excel_97_2000_XP);
            formats.add(OOoFormats.Text_CSV);
        }
        return formats;
    }

    
    private boolean isDoc(Document entry) {
        return (entry!=null && entry.getId().indexOf("/document%3A")!=-1);
    }
    
    private boolean isPresentation(Document entry) {
        return (entry!=null && entry.getId().indexOf("/presentation%3A")!=-1);
    }
    
    private boolean isSpreadsheet(Document entry) {
        return (entry!=null && entry.getId().indexOf("/spreadsheet%3A")!=-1);
    }

    public boolean hasList() {
        return (listOfDocuments!=null);
    }

    public Date parseDate(String date) throws ParseException {
        return parseDf.parse(date);
    }
}

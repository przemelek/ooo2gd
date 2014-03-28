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
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.http.HttpAuthToken;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
//import com.google.gdata.data.Category;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
//import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.util.AuthenticationException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
//import java.util.Map;
import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.ui.dialogs.CaptchaDialog;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GoogleDocsWrapper implements Wrapper {

        private class Entry {
                String type;
                String id;
                String contentSrc;
                String contentType;
                int size;
                String title;
                String updated;
//                String documentLink;
                String editMediaLink;
                List<String> categories = new ArrayList<String>();
        }


        public static final OOoFormats[] SUPPORTED_FORMATS = {OOoFormats.Text,OOoFormats.HTML_Document_OpenOfficeorg_Writer,OOoFormats.OpenDocument_Text,
                                                              OOoFormats.OpenOfficeorg_10_Text_Document,OOoFormats.Microsoft_Word_97_2000_XP,
                                                              OOoFormats.Microsoft_Word_95,OOoFormats.Microsoft_Word_60,OOoFormats.Rich_Text_Format,
                                                              OOoFormats.Microsoft_PowerPoint_97_2000_XP,OOoFormats.Microsoft_Excel_97_2000_XP,
                                                              OOoFormats.Microsoft_Excel_95,OOoFormats.Microsoft_Excel_50,OOoFormats.OpenDocument_Spreadsheet,
                                                              OOoFormats.Text_CSV,OOoFormats.OpenDocument_Drawing_Impress};
        private static final OOoFormats[] UNCONVERTABLE_FORMATS = {OOoFormats.Text_CSV,OOoFormats.OpenDocument_Drawing_Impress};
        // Yo Google! Sad that you didn't publish statistics for 3rd party programs using Google Docs API
        // btw. Cracow office looks nice from inside ;-)
	public static final String APP_NAME = "RMK-OpenOffice.orgDocsUploader-"+Configuration.getVersionStr();
	public static final String DOCS_FEED = "https://docs.google.com/feeds/documents/private/full";
//        public static final String DOCS_FEED = "https://docs.google.com/feeds/default/private/full";
	private DocsService service;
        private SpreadsheetService spreadsheetService;
        private Creditionals creditionals;
        private boolean isLogedIn = false;
        private static List<Document> listOfDocuments;
//        private static Map<Document,DocumentListEntry> doc2Entry = null;
//        private static Map<Document,Entry> doc2Entry = null;
        private DateFormat parseDf;
        private Set<String> supportedMimeTypes;
        
        public GoogleDocsWrapper() {
            //        df = new SimpleDateFormat("yyMMddHHmmssZ");
//            parseDf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            parseDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
//            parseDf = DateFormat.getDateTimeInstance();
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
	
	public UploadUpdateStatus upload(String path,String documentTitle,String mimeType,boolean convert) throws IOException, ServiceException {
              boolean result = false;              
              return uploadFile(path, documentTitle,mimeType,convert);
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
                name = name.replaceAll("[^\\w]","_");
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

        private UploadUpdateStatus uploadFile(final String path, final String documentTitle, final String mimeType, final boolean convert) throws IOException, MalformedURLException, ServiceException {
            if (convert) {
              DocumentEntry newDocument = new DocumentEntry();
              File documentFile = getFileForPath(path);
              newDocument.setFile(documentFile,mimeType);
              newDocument.setTitle(new PlainTextConstruct(documentTitle));
              URL documentListFeedUrl = new URL(DOCS_FEED);
              DocumentListEntry uploaded = service.insert(documentListFeedUrl, 
                  newDocument);
              return new UploadUpdateStatus(true, uploaded.getDocumentLink().getHref());
            } else {
                performResumableUpload(path,"https://docs.google.com/feeds/upload/create-session/default/private/full?convert=false", mimeType, documentTitle);
//                performResumableUpload(path,"http://docs.google.com/feeds/upload/create-session/default/private/full?convert=false", mimeType, documentTitle);
                return new UploadUpdateStatus(true, null);
            }
	}
	
        public List<Document> getListOfDocs(boolean useCachedListIfPossible) throws IOException, ServiceException {
            if (!useCachedListIfPossible || listOfDocuments==null) {
                Configuration.log("Try to get list of docs...");                
		List<Document> listOfDocuments = new LinkedList<Document>();
//                if (doc2Entry==null) {
//                    Configuration.log("Create new doc2Entry");
////                    doc2Entry = new HashMap<Document, DocumentListEntry>();
//                    doc2Entry = new HashMap<Document, Entry>();
//                }
//                doc2Entry.clear();

                // -- new way start

                if (supportedMimeTypes==null) {
                    supportedMimeTypes = new HashSet<String>();
                    for (OOoFormats format:SUPPORTED_FORMATS) {
                        supportedMimeTypes.add(format.getMimeType());
                    }
                }

		String listUrlStr = "https://docs.google.com/feeds/default/private/full";
		URL listUrl = new URL(listUrlStr);

              
		HttpURLConnection conn_ = (HttpURLConnection)listUrl.openConnection();
                HttpAuthToken authToken = (HttpAuthToken)service.getAuthTokenFactory().getAuthToken();
                String header = authToken.getAuthorizationHeader(listUrl, "GET");
//		conn_.setRequestProperty("Authorization", "GoogleLogin auth="+auth);
                conn_.setRequestProperty("Authorization", header);
		conn_.setRequestProperty("GData-Version", "3.0");
		conn_.connect();


		SAXParserFactory factory = SAXParserFactory.newInstance();

                try {

                    SAXParser parser = factory.newSAXParser();
                    XMLReader reader  = parser.getXMLReader();

                    final List<Entry> entries = new ArrayList<Entry>();

                    reader.setContentHandler(new DefaultHandler() {
                            private Entry entry;
                            private String str;
                            @Override
                            public void startElement(String uri, String localName,
                                            String qName, Attributes attributes) throws SAXException {
                                    super.startElement(uri, localName, qName, attributes);
                                    if ("entry".equals(qName)) {
                                            entry = new Entry();
                                    }
                                    if (entry!=null) {
                                            if ("content".equals(qName)) {
                                                    entry.contentType=attributes.getValue("type");
                                                    entry.contentSrc=attributes.getValue("src");
                                            } else if ("link".equals(qName)) {
                                                    if (attributes.getIndex("title")!=-1) {
                                                        entry.categories.add(attributes.getValue("title"));
                                                    } else if (attributes.getIndex("href")!=-1) {
//                                                        if ("edit-media".equals(attributes.getValue("rel"))) {
//                                                            entry.editMediaLink=attributes.getValue("href");
//                                                        }
                                                        if (attributes.getValue("rel").indexOf("resumable-edit-media")!=-1) {
                                                            entry.editMediaLink=attributes.getValue("href");
                                                        }

                                                    }
                                            }
                                    }
                            }

                            @Override
                            public void endElement(String uri, String localName, String qName)
                                            throws SAXException {
                                    super.endElement(uri, localName, qName);
                                    if ("entry".equals(qName)) {
                                            entries.add(entry);
                                            entry=null;
                                    }
                                    if (entry!=null) {
                                            if ("id".equals(qName)) {
                                                    entry.id=str;
                                            } else if ("title".equals(qName)) {
                                                    entry.title=str;
                                            } else if ("updated".equals(qName)) {
                                                    entry.updated=str;
                                            } else if ("gd:quotaBytesUsed".equals(qName)) {
                                                    entry.size = Integer.valueOf(str);
                                            } else if ("gd:resourceId".equals(qName)) {
                                                    entry.type = str.split(":")[0];
                                            }
                                    }
                                    str=null;
                            }

                            @Override
                            public void characters(char[] ch, int start, int length)
                                            throws SAXException {
                                    super.characters(ch, start, length);
                                    if (str!=null) {
                                        str=str+new String(ch,start,length);
                                    } else {
                                        str = new String(ch,start,length);
                                    }
                            }
                    });

                    reader.parse(new InputSource(conn_.getInputStream()));

//                    for (Entry entry:entries) {
//                            System.out.println(entry.title+" "+entry.contentType+" "+entry.id+" "+entry.contentSrc+" "+entry.categories);
//                    }

                    for (Entry entry:entries) {
                        Document docEntry = new Document();
                        docEntry.setDocumentLink(entry.contentSrc);
                        docEntry.setId(entry.id);
                        docEntry.setTitle(entry.title);
                        docEntry.setUpdated(entry.updated);
                        docEntry.setConvertable(!"file".equals(entry.type));
                        docEntry.setEditMediaLink(entry.editMediaLink);
                        docEntry.setContentType(entry.contentType);
                        for (String cat:entry.categories) {
                            docEntry.addFolder(cat);
                        }

                        if (isDoc(docEntry) || isSpreadsheet(docEntry) || isPresentation(docEntry)) {
                            listOfDocuments.add(docEntry);
                        } else if (supportedMimeTypes.contains(entry.contentType)) {
                            listOfDocuments.add(docEntry);
                        }

//                        listOfDocuments.add(docEntry);
                    }


                } catch (Exception e) {
                    
                }

                // -- new way end

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
            System.out.println(entry.getId());
            String id = entry.getId().split("%3A")[1];
            String type = entry.getId().split("%3A")[0];
            type = type.substring(type.lastIndexOf("/")+1);
            String entryLink = entry.getDocumentLink();
            String uriStr = entryLink.substring(0,entryLink.lastIndexOf("/")+1).replace("http:","https:");
            System.out.println("entryLink:"+entryLink);
            System.out.println("uriStr:"+uriStr);
            String formatStr;
            boolean newDoc = uriStr.equals("https://docs.google.com/document/");
            if ("document".equals(type)) {
                formatStr = format.getFileExtension();
//                uriStr+="feeds/download/documents/Export?docID="+id+"&exportFormat="+formatStr;
                uriStr+="Export?docID="+id+"&exportFormat="+formatStr;
                if (newDoc) {
                    uriStr = "https://docs.google.com/document/export?format="+formatStr+"&id="+id;
                }
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
//                uriStr+= "feeds/download/spreadsheets/Export?key="+id+"&fmcmd="+formatStr;
                uriStr+= "Export?key="+id+"&fmcmd="+formatStr;
            } else if ("presentation".equals(type)) {
                // not sure why, but it looks that Export servlet URL is now
                // http://docs.google.com/present/export?format=ppt&id=
                formatStr = format.getFileExtension();
//                uriStr+="feeds/download/presentations/Export?docID="+id+"&exportFormat="+formatStr;
//                uriStr="https://docs.google.com/present/export?format="+formatStr+"&id="+id;
                uriStr="https://docs.google.com/present/export?format="+formatStr+"&id="+id;
            }
            System.out.println(uriStr);
            return new URI(uriStr);
        }	
	

        public URI getUriForEntryInBrowser(final Document entry) throws URISyntaxException {
            String uriStr = "";
            uriStr = entry.getDocumentLink();
            return new URI(uriStr);
        }

//        public boolean neededConversion(String path) {
//            if (path.toLowerCase().endsWith(".odp")) {
//                return true;
//            }
//            return false;
//        }
        
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

        public UploadUpdateStatus update(String path, String docId,String mimeType)  throws Exception {
            List<Document> docs = getListOfDocs(true);
//            Map<String,Document> mapOfDocs = new HashMap<String,Document>();
            Document docToUpdate = null;
            for (Document doc:docs) {
                if (doc.getDocumentLink().equals(docId)) {
                    docToUpdate = doc;
                    break;
                }
            }
            if (docToUpdate!=null) {

//                String entryLink = docToUpdate.getDocumentLink();

//                String uriStr = entryLink.substring(0,entryLink.lastIndexOf("/")+1).replace("http:","https:");

                final String uploadUrlStr = docToUpdate.getEditMediaLink();
                performResumableUpload(path, uploadUrlStr, mimeType, null);

                return new UploadUpdateStatus(true, uploadUrlStr);
            } else {
                Configuration.log(path+" will not be updated.");
                return new UploadUpdateStatus(false, null);
            }
        }

    private void performResumableUpload(String path, final String uploadUrlStr, String mimeType,String title) throws MalformedURLException, ProtocolException, IOException {
        //                String uriStr = uploadUrlStr;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(getFileForPath(path));
        byte[] buf = new byte[8192];
        int read = -1;
        do {
            read = fis.read(buf);
            baos.write(buf, 0, read);
        } while (read == 8192);
        fis.close();
        URL uploadUrl = new URL(uploadUrlStr);
        HttpURLConnection conn = (HttpURLConnection) uploadUrl.openConnection();
        if (title==null) {
            conn.setRequestMethod("PUT");
        } else {
            conn.setRequestMethod("POST");
        }
        HttpAuthToken authToken = (HttpAuthToken) service.getAuthTokenFactory().getAuthToken();
        String header = authToken.getAuthorizationHeader(uploadUrl, conn.getRequestMethod());
        conn.setRequestProperty("Authorization", header);
        conn.setRequestProperty("GData-Version", "3.0");
        conn.setRequestProperty("Content-Type", mimeType);
        if (title!=null) {
            conn.setRequestProperty("Slug", title);
        } else {
            conn.setRequestProperty("If-Match", "*");
        }
        long contentLength = baos.size();        
        conn.setRequestProperty("X-Upload-Content-Length", "" + contentLength);
        conn.setRequestProperty("X-Upload-Content-Type", "" + mimeType);
//        conn.setRequestProperty("Content-Length", "0");
        conn.setFixedLengthStreamingMode(0);
        conn.setDoOutput(true);
        conn.connect();

        BufferedReader br2_ = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line2_ = "";
        while ((line2_ = br2_.readLine()) != null) {
            System.out.println(line2_);
        }
        String location = conn.getHeaderField("Location");
        conn.disconnect();
        conn = (HttpURLConnection) new URL(location).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Length", "" + contentLength);
        conn.setRequestProperty("Content-Range", "bytes 0-" + (contentLength - 1) + "/" + contentLength);
        //                conn.setRequestProperty("X-Upload-Content-Type", ""+mimeType);
        conn.setDoOutput(true);
        conn.connect();
        conn.getOutputStream().write(baos.toByteArray());
        BufferedReader br2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line2 = "";
        while ((line2 = br2.readLine()) != null) {
            System.out.println(line2);
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

    public boolean isConversionObligatory() {
        return false;
    }

    public boolean isConversionPossible(OOoFormats format) {
        boolean a = Arrays.asList(SUPPORTED_FORMATS).contains(format);
        boolean b = Arrays.asList(UNCONVERTABLE_FORMATS).contains(format);
        return !b && a;
    }


//    public static final void main(String... s) throws FileNotFoundException, IOException {
////        GoogleDocsWrapper gdw = new GoogleDocsWrapper();
////        gdw.getFileForPath("/home/benjamin_long/Documents/RAMBLINGS.odt");
//        String name = "A.b.;_a1223422.oxt";
//        String name1 = name.replaceAll("[^a-z,A-Z,0-9,_]","_");
//        String name2 = name.replaceAll("[^\\w]","_");
//        System.out.println(name);
//        System.out.println(name1);
//        System.out.println(name2);
//    }


}

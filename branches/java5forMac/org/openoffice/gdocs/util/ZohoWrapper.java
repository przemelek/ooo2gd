package org.openoffice.gdocs.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.openoffice.gdocs.configuration.Configuration;

public class ZohoWrapper implements Wrapper {
	
	public final static String API_KEY = "5836e626337ffd39bfd3a8114e4956e5";
        private List<org.openoffice.gdocs.util.Document> listOfDocuments;
        
	private class ZohoDownloader extends Downloader {
		public ZohoDownloader(URI source, OutputStream out,Wrapper wrapper) throws MalformedURLException {
			super(source,out,wrapper);
		}
		
		public ZohoDownloader(URI source, String destFileURI,Wrapper wrapper) throws MalformedURLException, URISyntaxException {
			super(source,destFileURI,wrapper);
		}
		
		@Override
		protected void setAthenticationHeader(HttpURLConnection conn) {
			// empty implementation, we don't need it here :-)
		}
	}
	
	private class ZohoDocument {
		private String documentId;
		private String documentName;
		private String version;
		private String lastModifiedTime;
		private String lastModifiedBy;
		private String writePermission;
		private String document_name_url;
		private String shared_users;
		private String document_locked;
		private String document_blogged;
		private String authorName;
		private String created_date;
		private String category;
		public String getDocumentId() {
			return documentId;
		}
		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}
		public String getDocumentName() {
			return documentName;
		}
		public void setDocumentName(String documentName) {
			this.documentName = documentName;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getLastModifiedTime() {
			return lastModifiedTime;
		}
		public void setLastModifiedTime(String lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
		}
		public String getLastModifiedBy() {
			return lastModifiedBy;
		}
		public void setLastModifiedBy(String lastModifiedBy) {
			this.lastModifiedBy = lastModifiedBy;
		}
		public String getWritePermission() {
			return writePermission;
		}
		public void setWritePermission(String writePermission) {
			this.writePermission = writePermission;
		}
		public String getDocument_name_url() {
			return document_name_url;
		}
		public void setDocument_name_url(String document_name_url) {
			this.document_name_url = document_name_url;
		}
		public String getShared_users() {
			return shared_users;
		}
		public void setShared_users(String shared_users) {
			this.shared_users = shared_users;
		}
		public String getDocument_locked() {
			return document_locked;
		}
		public void setDocument_locked(String document_locked) {
			this.document_locked = document_locked;
		}
		public String getDocument_blogged() {
			return document_blogged;
		}
		public void setDocument_blogged(String document_blogged) {
			this.document_blogged = document_blogged;
		}
		public String getAuthorName() {
			return authorName;
		}
		public void setAuthorName(String authorName) {
			this.authorName = authorName;
		}
		public String getCreated_date() {
			return created_date;
		}
		public void setCreated_date(String created_date) {
			this.created_date = created_date;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}		
	}
	
	private String ticket = "";
	
	public void login(Creditionals creditionals) throws Exception {
		String user = creditionals.getUserName();
		String password = creditionals.getPassword();
		String loginUrl = "https://accounts.zoho.com/login?servicename=ZohoWriter&FROM_AGENT=true&LOGIN_ID="+user+"&PASSWORD="+password;
		BufferedReader br = getBufferedReaderForUriString(loginUrl);
		String line = "";
		String ticket = null;
		while ((line=br.readLine())!=null) {
			System.out.println(line);
			if (line.startsWith("TICKET")) {
				String[] elems = line.split("=");
				ticket = elems[1];
				setTicket(ticket);
				break;
			}
		}
		if (ticket==null) {
			throw new AuthenticationException("Cannot authenticate");
		}
		System.out.println(ticket);
		br.close();
	}


	private BufferedReader getBufferedReaderForUriString(String loginUrl)
			throws MalformedURLException, URISyntaxException, IOException {
		ByteArrayInputStream bais = getByteArrayInputStreamForUriString(loginUrl);
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		return br;
	}


	private ByteArrayInputStream getByteArrayInputStreamForUriString(
			String loginUrl) throws MalformedURLException, URISyntaxException,
			IOException {
		ByteArrayOutputStream baos = getByteArrayOutputStreamForUriString(loginUrl);
		byte[] bytes = baos.toByteArray();		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		return bais;
	}


	private ByteArrayOutputStream getByteArrayOutputStreamForUriString(
			String loginUrl) throws MalformedURLException, URISyntaxException,
			IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Downloader downloader = new ZohoDownloader(new URI(loginUrl),baos,this);
		downloader.download();
		return baos;
	}
	
        
        private abstract class GetListHandler {
            private String url;
            public GetListHandler(String url) {
                this.url = url;
            }
            
            public List<ZohoDocument> getList() throws IOException, ServiceException, URISyntaxException, ParserConfigurationException, SAXException  {
		List<ZohoDocument> list = new LinkedList<ZohoDocument>();
		//String docsListURI = "https://export.writer.zoho.com/api/private/xml/documents?apikey="+API_KEY+"&ticket="+getTicket();
                String docsListURI = url;
		InputStream is = getByteArrayInputStreamForUriString(docsListURI);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(is);
		NodeList nodes = document.getFirstChild().getFirstChild().getFirstChild().getChildNodes();
		System.out.println(nodes.getLength());
		for (int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			System.out.println(i+" "+node.getTextContent());
			NodeList childNodes = node.getChildNodes();
			ZohoDocument entry = new ZohoDocument();
			for (int idx=0; idx<childNodes.getLength(); idx++) {				
				Node childNode = childNodes.item(idx);
				System.out.println(idx+" "+childNode.getNodeName()+" "+childNode.getTextContent());
				String nodeName = childNode.getNodeName();
				String content = childNode.getTextContent();		
                                handleNode(entry,nodeName,content);
                        }
			list.add(entry);
		}
		
		return list;                
            }

            protected abstract void handleNode(ZohoDocument entry, String nodeName, String content);            
        }

        private List<ZohoDocument> getListOfZohoPresentations() throws IOException, URISyntaxException, ServiceException, ParserConfigurationException, SAXException {
            String workbooksListURI = "http://show.zoho.com/api/private/xml/presentations?apikey="+API_KEY+"&ticket="+getTicket();
            return new GetListHandler(workbooksListURI) {

            @Override
                protected void handleNode(ZohoDocument entry, String nodeName, String content) {
                        if ("presentationId".equals(nodeName)) {
                                entry.setDocumentId(content);
                        } else if ("presentationId".equals(nodeName)) {
                                entry.setDocumentName(content);
                        } else if ("lastModifiedTime".equals(nodeName)) {
                                entry.setLastModifiedTime(content);
                        } else if ("lastModifiedBy".equals(nodeName)) {
                                entry.setLastModifiedBy(content);
                        }
                    
                }
                
            }.getList();
        }
                
        private List<ZohoDocument> getListOfZohoWorkbooks() throws IOException, URISyntaxException, ServiceException, ParserConfigurationException, SAXException {
            String workbooksListURI = "http://sheet.zoho.com/api/private/xml/books?apikey="+API_KEY+"&ticket="+getTicket();
            return new GetListHandler(workbooksListURI) {

            @Override
                protected void handleNode(ZohoDocument entry, String nodeName, String content) {
                        if ("workbookId".equals(nodeName)) {
                                entry.setDocumentId(content);
                        } else if ("workbookName".equals(nodeName)) {
                                entry.setDocumentName(content);
                        } else if ("lastModifiedTime".equals(nodeName)) {
                                entry.setLastModifiedTime(content);
                        } else if ("lastModifiedBy".equals(nodeName)) {
                                entry.setLastModifiedBy(content);
                        }
                    
                }
                
            }.getList();
        }
                               
	private List<ZohoDocument> getListOfZohoDocuments() throws IOException, ServiceException, URISyntaxException, ParserConfigurationException, SAXException {
		String docsListURI = "https://export.writer.zoho.com/api/private/xml/documents?apikey="+API_KEY+"&ticket="+getTicket();
                return new GetListHandler(docsListURI) {
                    @Override
                    protected void handleNode(ZohoDocument entry, String nodeName, String content) {
                        if ("documentId".equals(nodeName)) {
                                entry.setDocumentId(content);
                        } else if ("documentName".equals(nodeName)) {
                                entry.setDocumentName(content);
                        } else if ("version".equals(nodeName)) {
                                entry.setVersion(content);
                        } else if ("lastModifiedTime".equals(nodeName)) {
                                entry.setLastModifiedTime(content);
                        } else if ("lastModifiedBy".equals(nodeName)) {
                                entry.setLastModifiedBy(content);
                        } else if ("writePermission".equals(nodeName)) {
                                entry.setWritePermission(content);
                        } else if ("document_name_url".equals(nodeName)) {
                                entry.setDocument_name_url(content);
                        } else if ("shared_users".equals(nodeName)) {
                                entry.setShared_users(content);
                        } else if ("document_locked".equals(nodeName)) {
                                entry.setDocument_locked(content);
                        } else if ("document_blogged".equals(nodeName)) {
                                entry.setDocument_blogged(content);
                        } else if ("authorName".equals(nodeName)) {
                                entry.setAuthorName(content);
                        } else if ("created_date".equals(nodeName)) {
                                entry.setCreated_date(content);
                        } else if ("category".equals(nodeName)) {
                                entry.setCategory(content);
                        }
                    }
                }.getList();
        }
	
	
	public void downloadDocument(String documentId,String targetFileName) throws MalformedURLException, IOException, URISyntaxException {
		String documentUri = "https://export.writer.zoho.com/api/private/odt/download/"+documentId+"?apikey="+API_KEY+"&ticket="+getTicket();
		new ZohoDownloader(new URI(documentUri),targetFileName,this).download();
	}
	
    private int getStream(final InputStream is, OutputStream out) throws IOException {
        int progress = 0;
        byte[] buffer = new byte[1024*8];            
        int readCount;        
        while((readCount=is.read(buffer))>0) {
          out.write(buffer, 0, readCount);
          progress += readCount;
        }
        out.flush();
        out.close();
        out = null;
        return progress;
    }
   
	private boolean uploadDocumentForUrl(String sourceFileName,String documentName,String url,Map<String,String> parameters) throws IOException {
            //String uploadUri = "https://export.writer.zoho.com/api/private/xml/uploadDocument?apikey="+API_KEY+"&ticket="+getTicket();
            String uploadUri = url+"?apikey="+API_KEY+"&ticket="+getTicket();
            URL source = new URL(uploadUri);
	    HttpURLConnection.setDefaultAllowUserInteraction(true);
	    String fileName = sourceFileName.substring(sourceFileName.lastIndexOf("\\")+1);
	    
	    HttpURLConnection conn = (HttpURLConnection)source.openConnection();
	    String boundary = "AaB03x";
	    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
	    conn.setRequestProperty("User-Agent", "ooo2gd");
	    conn.setRequestMethod("POST");
	    
	    FileInputStream fis = new FileInputStream(sourceFileName);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    
	    long contentLength = getStream(fis,baos);
	    conn.setRequestProperty("Content-Length", ""+contentLength);
	    conn.setDoOutput(true);
	    conn.connect();
            
            if (parameters!=null) {
                for (Map.Entry<String,String> entry:parameters.entrySet()) {
                    conn.getOutputStream().write(("--"+boundary+"\r\n").getBytes());
                    conn.getOutputStream().write(("Content-Disposition: form-data; name=\""+entry.getKey()+"\"\r\n").getBytes());
                    conn.getOutputStream().write(("Content-Type: application/download\r\n").getBytes());
                    conn.getOutputStream().write(("\r\n").getBytes());
                    conn.getOutputStream().write((entry.getValue()+"\r\n").getBytes());
                }
            }                    
	    conn.getOutputStream().write(("--"+boundary+"\r\n").getBytes());
	    conn.getOutputStream().write(("Content-Disposition: form-data; name=\"content\"; filename=\""+fileName+"\"\r\n").getBytes());
	    conn.getOutputStream().write(("Content-Type: application/download\r\n").getBytes());
	    conn.getOutputStream().write(("\r\n").getBytes());
	    conn.getOutputStream().write(baos.toByteArray());
	    conn.getOutputStream().write(("\r\n--"+boundary+"--\r\n").getBytes());
	    conn.getOutputStream().flush();
	    conn.getOutputStream().close();
	    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
	    getStream(conn.getInputStream(), baos2);
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos2.toByteArray())));
            String answer = "";
            String line = "";
//		String documentId = null;
            boolean looksForCorrectAnswer = false;
            while ((line=br.readLine())!=null) {
                    System.out.println(line);
                    if (line.indexOf("documentId")!=-1) {
                        looksForCorrectAnswer = true;
                        //break;
                    }
                    if (line.indexOf("workbookId")!=-1) {
                        looksForCorrectAnswer = true;
                        //break;
                    }
                    if (line.indexOf("presentationId")!=-1) {
                        looksForCorrectAnswer = true;
                        //break;                            
                    }
                    answer+=line;
            }	    
	    
	    baos.close();
            if (!looksForCorrectAnswer) {
                Configuration.log("Cannot upload file "+sourceFileName);
                Configuration.log(answer);
                throw new IOException("Cannot upload file");                
            }
	    return looksForCorrectAnswer;
	}

        private boolean uploadDocument(String sourceFileName,String documentName) throws IOException {
            return uploadDocumentForUrl(sourceFileName, documentName,"https://export.writer.zoho.com/api/private/xml/uploadDocument",null);
        }        
        
        private boolean uploadSheet(String sourceFileName,String documentName) throws IOException {
            // https://sheet.zoho.com/api/private/xml/uploadbook xls|sxc|csv 
            //Map<String,String> parameters = new HashMap<String, String>();
//            map.put("workbookName",documentName);
//            map.put("")
            return uploadDocumentForUrl(sourceFileName, documentName,"http://sheet.zoho.com/api/private/xml/uploadbook",null);
        }

        private boolean uploadPresentation(String sourceFileName,String documentName) throws IOException {
            //Map<String,String> parameters = new HashMap<String, String>();
//            map.put("workbookName",documentName);
//            map.put("")
            return uploadDocumentForUrl(sourceFileName, documentName,"http://show.zoho.com/api/private/xml/uploadpresentation",null);
        }        
        
        private boolean isIn(String search,String... setOfPossibleValues) {
            for (String elementOfSet:setOfPossibleValues) {
                if (search.equals(elementOfSet)) return true;
            }
            return false;
        }
        
        public boolean upload(String sourceFileName,String documentName) throws IOException {
            // https://export.writer.zoho.com/api/private/xml/uploadDocument DOC/ODT/RTF
            // https://sheet.zoho.com/api/private/xml/uploadbook xls|sxc|csv            
            // https://show.zoho.com/api/private/xml/uploadpresentation ppt|pps|sxi|odp
            String fileExt = sourceFileName.substring(sourceFileName.lastIndexOf(".")+1).toLowerCase();
            if (isIn(fileExt,"doc","odt","rtf")) {
                return uploadDocument(sourceFileName, documentName);                
            } else if (isIn(fileExt,"xls","sxc","csv","ods")) {
                return uploadSheet(sourceFileName, documentName);
            } else if (isIn(fileExt,"ppt","pps","sxi","odp")) {
                return uploadPresentation(sourceFileName, documentName);
            }
            return false;
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
        
        private void fillListWithDocuments(List<org.openoffice.gdocs.util.Document> entries) throws ParserConfigurationException, URISyntaxException, IOException, ServiceException, SAXException {
            List<ZohoDocument> list = getListOfZohoDocuments();
            for (ZohoDocument doc : list) {
                org.openoffice.gdocs.util.Document docEntry = new org.openoffice.gdocs.util.Document();
                docEntry.setDocumentLink("https://export.writer.zoho.com/api/private/odt/download/" + doc.getDocumentId() + "?apikey=" + API_KEY + "&ticket=" + getTicket());
                docEntry.setId(doc.getDocumentId());
                docEntry.setTitle(doc.getDocumentName()+".odt");                                
                docEntry.setUpdated(new Date(Long.valueOf(doc.getLastModifiedTime())).toLocaleString());
                entries.add(docEntry);
            }
        }
        
        private void fillListWithWorkbooks(List<org.openoffice.gdocs.util.Document> entries) throws ParserConfigurationException, URISyntaxException, IOException, ServiceException, SAXException {
            List<ZohoDocument> list = getListOfZohoWorkbooks();
            for (ZohoDocument doc : list) {
                org.openoffice.gdocs.util.Document docEntry = new org.openoffice.gdocs.util.Document();                
                docEntry.setDocumentLink("http://sheet.zoho.com/api/private/ods/download/" + doc.getDocumentId() + "?apikey=" + API_KEY + "&ticket=" + getTicket());
                docEntry.setId(doc.getDocumentId());
                docEntry.setTitle(doc.getDocumentName()+".ods");
                docEntry.setUpdated(new Date(Long.valueOf(doc.getLastModifiedTime())).toLocaleString());
                entries.add(docEntry);
            }
        }        

        // I don't know how to implement it :-)
        private void fillListWithPresentations(List<org.openoffice.gdocs.util.Document> entries) throws ParserConfigurationException, URISyntaxException, IOException, ServiceException, SAXException {
            List<ZohoDocument> list = getListOfZohoPresentations();
            for (ZohoDocument doc : list) {
                org.openoffice.gdocs.util.Document docEntry = new org.openoffice.gdocs.util.Document();
                docEntry.setDocumentLink("http://sheet.zoho.com/api/private/xls/download/" + doc.getDocumentId() + "?apikey=" + API_KEY + "&ticket=" + getTicket());
                docEntry.setId(doc.getDocumentId());
                docEntry.setTitle(doc.getDocumentName());
                docEntry.setUpdated(doc.getLastModifiedTime());
                entries.add(docEntry);
            }
        }                
        
        public List<org.openoffice.gdocs.util.Document> getListOfDocs(boolean useCachedListIfPossible) throws IOException, ServiceException, URISyntaxException, ParserConfigurationException, SAXException {
            if (!useCachedListIfPossible || listOfDocuments==null) {
                listOfDocuments = new ArrayList<org.openoffice.gdocs.util.Document>();
                fillListWithDocuments(listOfDocuments);
                fillListWithWorkbooks(listOfDocuments);
            }
            return listOfDocuments;
        }
        
	private void setTicket(String ticket) {
		this.ticket = ticket;		
	}
	
	private String getTicket() {
		return this.ticket;
	}

        public boolean neeedConversion(String path) {
            return false;
        }

        public String closestSupportedFormat(String path) {
            return path.substring(path.lastIndexOf(".")+1).toLowerCase();
        }

        public String getSystem() {
            return "Zoho";
        }

        public URI getUriForEntry(org.openoffice.gdocs.util.Document entry) throws URISyntaxException {
            return new URI(entry.getDocumentLink());
        }

        public URI getUriForEntryInBrowser(org.openoffice.gdocs.util.Document entry) throws URISyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Downloader getDownloader(URI uri, String documentUrl) throws URISyntaxException, MalformedURLException {
            //String documentUri = "https://export.writer.zoho.com/api/private/odt/download/"+documentId+"?apikey="+API_KEY+"&ticket="+getTicket();
            return new ZohoDownloader(uri,documentUrl,this);
        }

        public boolean updateSupported() {
            return false;
        }

        public boolean update(String path, String docId) throws IOException {
            docId = docId.substring(0,docId.indexOf("?"));
            docId = docId.substring(docId.lastIndexOf("/")+1);
            String updateUrl = "http://export.writer.zoho.com/api/private/xml/saveDocument/"+docId+"?apikey="+API_KEY+"&ticket="+getTicket();
            URL source = new URL(updateUrl);            
	    HttpURLConnection.setDefaultAllowUserInteraction(true);
	    //String fileName = path.substring(sourceFileName.lastIndexOf("\\")+1);	    
	    HttpURLConnection conn = (HttpURLConnection)source.openConnection();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();            
            FileInputStream fis = new FileInputStream(path);
	    conn.setRequestProperty("User-Agent", "ooo2gd");
	    conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/download");
	    baos.write("content=".getBytes());
            long contentLength = getStream(fis,baos);
	    conn.setRequestProperty("Content-Length", ""+contentLength);
	    conn.setDoOutput(true);
	    conn.connect();            
            OutputStream os = conn.getOutputStream();                        
            os.write(baos.toByteArray());
	    os.flush();
	    os.close();
            
	    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
	    getStream(conn.getInputStream(), baos2);	               
            
	    BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos2.toByteArray())));
            String answer = "";
            String line = "";
//		String documentId = null;
            boolean looksForCorrectAnswer = false;
            while ((line=br.readLine())!=null) {
                    System.out.println(line);
                    if (line.indexOf("documentId")!=-1) {
                        looksForCorrectAnswer = true;
                        //break;
                    }
                    if (line.indexOf("workbookId")!=-1) {
                        looksForCorrectAnswer = true;
                        //break;
                    }
                    if (line.indexOf("presentationId")!=-1) {
                        looksForCorrectAnswer = true;
                        //break;                            
                    }
                    answer+=line;
            }	    
	    
	    baos.close();
            if (!looksForCorrectAnswer) {
                Configuration.log("Cannot update file "+path);
                Configuration.log(answer);
                throw new IOException("Cannot update file");                
            }
	    return looksForCorrectAnswer;            
        }
                
	public static void main(String[] args) throws Exception {
		ZohoWrapper zohoWrapper = new ZohoWrapper();
                Creditionals creds = new Creditionals("", "");
		zohoWrapper.login(creds);
		//List<ZohoDocument> list = zohoWrapper.getListOfDocuments();
                //List<ZohoDocument> list2 = zohoWrapper.getListOfZohoWorkbooks();
                for (org.openoffice.gdocs.util.Document doc:zohoWrapper.getListOfDocs(false)) {
                    System.out.println(doc.getDocumentLink());
                }
//		for (ZohoDocument document:list) {
//			//zohoWrapper.downloadDocument(document.getDocumentId(), document.getDocumentName()+".odt");
//		}                
//		System.out.println(zohoWrapper.upload("e:\\eksportTest.odt","eksportTest.odt"));
	}
}

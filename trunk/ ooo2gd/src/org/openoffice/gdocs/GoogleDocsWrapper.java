// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs;

import java.io.File;
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
	
        public GoogleDocsWrapper() {
        }
        
        public DocsService getService() {
            return service;
        }
        
	public void login(String userName,String password) throws AuthenticationException {
		service = new DocsService(APP_NAME);
		service.setUserCredentials(userName,password);
	}
	
	public boolean upload(String path,String documentTitle) throws Exception {
              boolean result = false; 
              DocumentEntry newDocument = new DocumentEntry();
              File documentFile = new File(path);
              newDocument.setFile(documentFile);
              newDocument.setTitle(new PlainTextConstruct(documentTitle));
              URL documentListFeedUrl = new URL(DOCS_FEED);
              DocumentListEntry uploaded = service.insert(documentListFeedUrl, 
                  newDocument);
              result=true;
              return result;
	}
	
	public List<DocumentListEntry> getListOfDocs() {
		List<DocumentListEntry> list = new LinkedList<DocumentListEntry>();
		try {
			URL documentFeedUrl = new URL(DOCS_FEED); 
			DocumentListFeed feed = service.getFeed(documentFeedUrl,DocumentListFeed.class);
			list=feed.getEntries();
		} catch (Exception e) {
				
		}
		return list;
	}
	
	public List<String> list() {
		List<String> list = new LinkedList<String>();
		for (DocumentListEntry entry:getListOfDocs()) {
			list.add(entry.getTitle().getPlainText());	
		}
		return list;
	}
	
}

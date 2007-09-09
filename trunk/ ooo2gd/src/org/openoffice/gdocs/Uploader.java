package org.openoffice.gdocs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JDialog;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.util.AuthenticationException;
import org.openoffice.gdocs.ui.UploadDialog;
import org.openoffice.gdocs.ui.Uploading;

public class Uploader implements Runnable {	
	public static final String APP_NAME = "RMK OpenOffice.org Docs Uploader";
	public static final String DOCS_FEED = "http://docs.google.com/feeds/documents/private/full";
        private String pathName;
        private File file;
	private DocsService service;
	
        public Uploader(String pathName) {
            this.pathName = pathName;
            this.file=new File(pathName);
        }
        
        public Uploader(URI uri) {
            this.file=new File(uri);
            this.pathName=file.getName();
        }
        
	public void login(String userName,String password) throws AuthenticationException {
		service = new DocsService(APP_NAME);
		service.setUserCredentials(userName,password);
	}
	
	public boolean upload(String path,String documentTitle) throws Exception {
              boolean result = false; 
              DocumentEntry newDocument = new DocumentEntry();
              File documentFile = this.file;              
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
	        
        
        public void run() {
            UploadDialog form = new UploadDialog();
            String docName = new File(pathName).getName();
            form.setMessageText("File "+pathName+" will be uploaded to Google Docs");
            form.setDocumentTitle(docName);
            form.setModal(true);
            form.toFront();
            form.setVisible(true);
            Uploading uploading = new Uploading();
            if (form.getUpload()) {
                try {              
                    docName=form.getDocumentTitle();
                    uploading.setVisible(true);
                    login(form.getUserName(),form.getPassword());                    
                    upload(pathName,docName);
                    JOptionPane.showMessageDialog(null,"File Uploaded");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,"Problem: "+e.getMessage());
                }
                finally {
                    uploading.setVisible(false);
                }
                
            }
        
        }
        
	
}

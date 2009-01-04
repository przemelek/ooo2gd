// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.ui.dialogs;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.ui.models.DocumentsTableModel;
import org.openoffice.gdocs.util.Creditionals;
import org.openoffice.gdocs.util.Downloader;
import org.openoffice.gdocs.util.IOEvent;
import org.openoffice.gdocs.util.IOListener;
import org.openoffice.gdocs.util.Util;
import org.openoffice.gdocs.util.Document;
import org.openoffice.gdocs.util.Wrapper;

import com.google.gdata.util.AuthenticationException;
import com.sun.star.frame.XFrame;
import org.openoffice.gdocs.util.WrapperFactory;

/**
 *
 * @author  rmk
 */
public class ImportDialog extends JFrame {
	
  private final class ImportIOListener implements IOListener {
		private final String url;

		private final Uploading window;

		private ImportIOListener(String url, Uploading window) {
			this.url = url;
			this.window = window;
		}

		public void ioProgress(IOEvent ioEvent) {   
                    int progress = (int)((float)ioEvent.getCompletedSize()/(float)ioEvent.getTotalSize()*100.0);
                    window.setProgress(progress);
		    if (ioEvent.isCompleted()) {		        		        
		        try {                            
                            File docFile = new File(url);
                            String fName = docFile.getCanonicalPath();
                            String sLoadUrl = Util.fileNameToOOoURL(fName);
                            Util.openInOpenOffice(sLoadUrl, xFrame);
		        } catch (Exception e) {
                            Configuration.log(e);
		            JOptionPane.showMessageDialog(ImportDialog.this,Configuration.getResources().getString("PROBLEM_CANNOT_OPEN")+"\n"+e.getMessage());
		        } finally {
                            window.dispose();
                        }
		    }
		}        
	}

    private class OpenWrapper {
        private Wrapper wrapper;
        private Creditionals creditionals;

        public OpenWrapper(Wrapper wrapper) {    
            this.wrapper = wrapper;
            creditionals = loginPanel1.getCreditionals();
        }          
        
        public void doOpen(Document entry) throws AuthenticationException, URISyntaxException, IOException {
            donwloadTextDocument(entry, getWrapper());
        }
        
        public void open() {
        try {            
            wrapper.login(creditionals);
            Document entry = (((DocumentsTableModel)jTable1.getModel()).getEntry(jTable1.getSelectedRow()));
            doOpen(entry);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            Configuration.log(e);
            JOptionPane.showMessageDialog(null,Configuration.getResources().getString("INVALID_CREDITIONALS"));
        } catch (IOException e) {
            e.printStackTrace();
            Configuration.log(e);
            JOptionPane.showMessageDialog(null,Configuration.getResources().getString("CANNOT_OPEN_BROWSER"));
        } catch (Exception e) {
            e.printStackTrace();
            Configuration.log(e);
            JOptionPane.showMessageDialog(null,Configuration.getResources().getString("Problem:_")+e.getMessage());              
        }
        }
        public Wrapper getWrapper() { return wrapper; }
        public Creditionals getCreditionals() { return creditionals; }
    }  
  
  
    private abstract class OpenWithBrowserWrapper extends OpenWrapper {

        public OpenWithBrowserWrapper(Wrapper wrapper) {
            super(wrapper);
        }
        
        
        
        public void doOpen(Document entry) throws AuthenticationException, URISyntaxException, IOException {
            Desktop.getDesktop().browse(getUri(entry));
        }
        public abstract URI getUri(Document entry) throws URISyntaxException;
    }  
  
  private XFrame xFrame;
  private final String currentDocumentPath;
  private String system;
  
    /** Creates new form ImportDialog */
    public ImportDialog(java.awt.Frame parent, boolean modal, String currentDocumentPath,String system,XFrame frame) {
        super();
        this.xFrame = frame;
        this.system = system;
        initComponents();
        loginPanel1.setSystem(system);
        setTitle(Configuration.getStringFromResources("Import_from_Google_Docs", system));
        getListButton.setText(Configuration.getResources().getString("Get_list"));
        openButton.setText(Configuration.getResources().getString("Open"));
        openInBrowser.setText(Configuration.getResources().getString("OPEN_IN_BROWSER"));
        openViaBrowserButton.setText(Configuration.getResources().getString("OPEN_VIA_BROWSER"));
        closeButton.setText(Configuration.getResources().getString("Close"));
        jTable1.setModel(new DocumentsTableModel());
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.currentDocumentPath = currentDocumentPath;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        loginPanel1 = new org.openoffice.gdocs.ui.LoginPanel();
        jPanel2 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        getListButton = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        openButton = new javax.swing.JButton();
        openInBrowser = new javax.swing.JButton();
        openViaBrowserButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setTitle("Import from Google Docs");
        setFocusTraversalPolicyProvider(true);
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.add(loginPanel1);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        jPanel2.add(closeButton, new java.awt.GridBagConstraints());

        jLabel1.setText("<html><font size=\"1\">(c) <u><font color=\"blue\">Przemyslaw Rumik</font></u></font></html>");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jLabel1, gridBagConstraints);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        getListButton.setText("Get list");
        getListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getListButtonActionPerformed(evt);
            }
        });
        jSplitPane1.setTopComponent(getListButton);

        jSplitPane2.setDividerLocation(300);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setFocusCycleRoot(true);
        jSplitPane2.setPreferredSize(new java.awt.Dimension(300, 134));

        jPanel3.setMaximumSize(new java.awt.Dimension(71, 33));

        jPanel4.setLayout(new java.awt.BorderLayout());

        openButton.setText("Open");
        openButton.setEnabled(false);
        openButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        jPanel4.add(openButton, java.awt.BorderLayout.PAGE_START);

        openInBrowser.setText("Open in Browser");
        openInBrowser.setEnabled(false);
        openInBrowser.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        openInBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInBrowserActionPerformed(evt);
            }
        });
        jPanel4.add(openInBrowser, java.awt.BorderLayout.PAGE_END);

        openViaBrowserButton.setText("Open via Browser");
        openViaBrowserButton.setEnabled(false);
        openViaBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openViaBrowserButtonActionPerformed(evt);
            }
        });
        jPanel4.add(openViaBrowserButton, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel4);

        jSplitPane2.setRightComponent(jPanel3);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setMinimumSize(new java.awt.Dimension(200, 64));
        jScrollPane1.setViewportView(jTable1);

        jSplitPane2.setLeftComponent(jScrollPane1);

        jSplitPane1.setBottomComponent(jSplitPane2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        new OpenWrapper(WrapperFactory.getWrapperForCredentials(system)).open();
    }//GEN-LAST:event_openButtonActionPerformed

    private void donwloadTextDocument(final Document entry, final Wrapper wrapper) throws MalformedURLException, IOException, URISyntaxException, UnsupportedEncodingException, HeadlessException {
        final String documentUrl = this.currentDocumentPath +"/"+entry.getTitle();
        final URI uri = wrapper.getUriForEntry(entry);
        downloadURI(documentUrl, uri, wrapper);
    }

    private void downloadURI(final String documentUrl, final URI uri, final Wrapper wrapper) throws MalformedURLException, URISyntaxException {
        final Downloader downloader = wrapper.getDownloader(uri, 
            documentUrl);
        final Uploading progressWindow = new Uploading();
        progressWindow.setMessage(wrapper.getSystem()+" -> OpenOffice.org");
        progressWindow.setVisible(true);            
        progressWindow.showProgressBar();
        downloader.addIOListener(new ImportIOListener(documentUrl, progressWindow));
        downloader.start();
    }

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        try {
            Desktop.getDesktop().browse(new URI("http://przemelek.googlepages.com/kontakt"));
        } catch (Exception e) {
            // OK, it's not crutial problem, so we ignore it ;-)'
        }
    }//GEN-LAST:event_jLabel1MouseClicked
        
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed
    
    
    private void getListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getListButtonActionPerformed
        final Wrapper wrapper = WrapperFactory.getWrapperForCredentials(system);
        try {
            wrapper.login(loginPanel1.getCreditionals());
            jTable1.setEnabled(true);
            DocumentsTableModel dtm = new DocumentsTableModel();                    
            for (Document entry:wrapper.getListOfDocs(false)) {
                dtm.add(entry);
            }
            jTable1.setModel(dtm);
            jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    Document entry = (((DocumentsTableModel)jTable1.getModel()).getEntry(jTable1.getSelectedRow()));
                    boolean googleAppsAccount = entry.getDocumentLink().indexOf("/a/")!=-1;
                    if ( entry.getId().indexOf("/spreadsheet%3A")!=-1 ) {
                        setButtonsEnable(false, false, true);
                    } else {
                    	setButtonsEnable(!googleAppsAccount, true, true);                                      
                    }
                }
				private void setButtonsEnable(boolean openState, boolean openViaBrowserState, boolean openInBrowserState) {
					openButton.setEnabled(openState);
					openViaBrowserButton.setEnabled(openViaBrowserState);
					openInBrowser.setEnabled(openInBrowserState);
				}
            });                
        } catch (Exception e) {
            e.printStackTrace();            
            Configuration.log(Configuration.getResources().getString("Problem:_")+e.getMessage());
            Configuration.log(e);
            JOptionPane.showMessageDialog(this,Configuration.getResources().getString("Problem:_")+e.getMessage());
        }

        this.repaint();
    }//GEN-LAST:event_getListButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    private void openViaBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openViaBrowserButtonActionPerformed
        new OpenWithBrowserWrapper(WrapperFactory.getWrapperForCredentials(system)) {

            @Override
            public URI getUri(Document entry) throws URISyntaxException {
                return getWrapper().getUriForEntry(entry);
            }
        }.open();
}//GEN-LAST:event_openViaBrowserButtonActionPerformed

    private void openInBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInBrowserActionPerformed
        new OpenWithBrowserWrapper(WrapperFactory.getWrapperForCredentials(system)) {

            @Override
            public URI getUri(Document entry) throws URISyntaxException {
                return getWrapper().getUriForEntryInBrowser(entry);
            }
        }.open();
    }//GEN-LAST:event_openInBrowserActionPerformed
    
    public static void main(String[] args) {
        new ImportDialog(null, true, ".", "Google Docs", null).setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton getListButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTable jTable1;
    private org.openoffice.gdocs.ui.LoginPanel loginPanel1;
    private javax.swing.JButton openButton;
    private javax.swing.JButton openInBrowser;
    private javax.swing.JButton openViaBrowserButton;
    // End of variables declaration//GEN-END:variables

}

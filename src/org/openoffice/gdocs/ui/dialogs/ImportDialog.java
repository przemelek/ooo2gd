// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.ui.dialogs;

import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.util.List;
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
import org.openoffice.gdocs.util.Document;
import org.openoffice.gdocs.util.Downloader;
import org.openoffice.gdocs.util.IOEvent;
import org.openoffice.gdocs.util.IOListener;
import org.openoffice.gdocs.util.Util;
import org.openoffice.gdocs.util.Wrapper;
import org.openoffice.gdocs.util.WrapperFactory;

import com.google.gdata.util.AuthenticationException;
import com.sun.star.frame.XFrame;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.lang.reflect.Method;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import org.openoffice.gdocs.util.OOoFormats;

/**
 *
 * @author  rmk
 */
public class ImportDialog extends JFrame {

    private void addFolderLabel(String color, String bgColor, final String folderName, final DocumentsTableModel dtm) {
        String text = "<html><body><font bgcolor=\"" + bgColor + "\" color=\"" + color + "\">&nbsp;[" + ("".equals(folderName)?"All":folderName) + "]&nbsp;</font></body></html>";
        JLabel label = new JLabel(text);
        label.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                dtm.setFolderFilter(folderName);
                dtm.fireTableDataChanged();
            }
        });
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        foldersPanel.add(label);
    }
  private final class ImportIOListener implements IOListener {
		private final String url;
                private boolean openAfterDownload;

		private final Uploading window;

		private ImportIOListener(String url, Uploading window, boolean openAfterDownload) {
			this.url = url;
			this.window = window;
                        this.openAfterDownload=openAfterDownload;
		}

		public void ioProgress(IOEvent ioEvent) {
                    if (ioEvent.getThrowable()!=null) {
                        window.dispose();
                        JOptionPane.showMessageDialog(ImportDialog.this,ioEvent.getMessage());
                    } else {
                        int progress = (int)((float)ioEvent.getCompletedSize()/(float)ioEvent.getTotalSize()*100.0);
                        window.setProgress(progress);
                        if (ioEvent.isCompleted()) {
                            try {
                                if (openAfterDownload) {
                                    File docFile = new File(url);
                                    String fName = docFile.getCanonicalPath();
                                    Configuration.modifyGlobalMapOfFiles(url,docFile.length(),docFile.lastModified());
                                    Util.openInOpenOffice(ImportDialog.this, fName,xFrame);
                                }
                            } catch (Exception e) {
                                Configuration.log(e);
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(ImportDialog.this,Configuration.getResources().getString("PROBLEM_CANNOT_OPEN")+"\n"+e.getLocalizedMessage());
                            } finally {
                                window.dispose();
                            }
                        }
                    }
		}        
	}

    private class OpenWrapper {
        private Wrapper wrapper;
        private Creditionals creditionals;
        private boolean openAfterDownload;
        
        public OpenWrapper(Wrapper wrapper, final boolean openAfterDownload) {    
            this.wrapper = wrapper;
            creditionals = loginPanel1.getCreditionals();
            this.openAfterDownload=openAfterDownload;
        }          
        
        public void doOpen(Document entry) throws AuthenticationException, URISyntaxException, IOException {
            donwloadTextDocument(entry, getWrapper(),openAfterDownload);
        }
        
        public void open() {
        try {            
            wrapper.login(creditionals);
            Document entry = (((DocumentsTableModel)jTable1.getModel()).getEntry(jTable1.getSelectedRow()));
            doOpen(entry);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            Configuration.log(e);
            JOptionPane.showMessageDialog(ImportDialog.this,Configuration.getResources().getString("INVALID_CREDITIONALS"));
        } catch (IOException e) {
            e.printStackTrace();
            Configuration.log(e);
            JOptionPane.showMessageDialog(ImportDialog.this,Configuration.getResources().getString("CANNOT_OPEN_BROWSER"));
        } catch (Exception e) {
            e.printStackTrace();
            Configuration.log(e);
            JOptionPane.showMessageDialog(ImportDialog.this,Configuration.getResources().getString("Problem:_")+e.getMessage());              
        }
        }
        public Wrapper getWrapper() { return wrapper; }
        public Creditionals getCreditionals() { return creditionals; }
    }  
  
  
    private abstract class OpenWithBrowserWrapper extends OpenWrapper {

        public OpenWithBrowserWrapper(Wrapper wrapper) {
            super(wrapper,true);
        }
        
        
        
        public void doOpen(Document entry) throws AuthenticationException, URISyntaxException, IOException {
//            Desktop.getDesktop().browse(getUri(entry));            
            Util.openBrowserForURL(ImportDialog.this, getUri(entry).toString());
        }
        public abstract URI getUri(Document entry) throws URISyntaxException;
    }  
  
  private XFrame xFrame;
  private final String currentDocumentPath;
  private String system;
  
    /** Creates new form ImportDialog */
    public ImportDialog(java.awt.Frame parent, boolean modal, String currentDocumentPath,String system,XFrame frame) {
        super(Configuration.getStringFromResources("Import_from_Google_Docs", system));
        this.xFrame = frame;
        this.system = system;
        initComponents();
        foldersPanel.setVisible(false);
        this.setSize(600, 450);        
        loginPanel1.setSystem(system);
        getListButton.setText(Configuration.getResources().getString("Get_list"));
        openButton.setText(Configuration.getResources().getString("Open"));
        openInBrowser.setText(Configuration.getResources().getString("OPEN_IN_BROWSER"));
        openViaBrowserButton.setText(Configuration.getResources().getString("OPEN_VIA_BROWSER"));
        closeButton.setText(Configuration.getResources().getString("Close"));
        jLabel2.setText(Configuration.getResources().getString("Filter"));
        jTable1.setModel(new DocumentsTableModel());
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        formatChoosePanel.setVisible(false);        
        formatChooserLabel.setText(Configuration.getResources().getString("As"));
        autoUpdate.setSelected(Configuration.isDefaultAutoUpdate());
        this.currentDocumentPath = currentDocumentPath;
        if (System.getProperty("java.specification.version").compareTo("1.5")>0) {
            Configuration.log("At least Java 1.6 :-)");
            try {
                    Configuration.log("Will try to add sorting to list of documents");
                    Method method = jTable1.getClass().getMethod("setAutoCreateRowSorter", boolean.class);
                    method.invoke(jTable1, true);
                } catch (Exception ex) {
                    Configuration.log(ex);
                }
        }
        Creditionals creditionals = loginPanel1.getCreditionals();
        Wrapper wrapper = WrapperFactory.getWrapperForCredentials(system);
        if (wrapper.hasList()) {
            try {
                fillListOfDocuments(wrapper,wrapper.getListOfDocs(true));
            } catch (Exception e) {
                // OK, this means that we were not able to obtain current docs list
            }
        }
//        jSplitPane2.setDividerLocation(0.7);
        Configuration.hideWaitWindow();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        googleDocsWrapper1 = new org.openoffice.gdocs.util.GoogleDocsWrapper();
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
        openViaBrowserButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        openInBrowser = new javax.swing.JButton();
        formatChoosePanel = new javax.swing.JPanel();
        autoUpdate = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        formatChooserLabel = new javax.swing.JLabel();
        formatComboBox = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        filterText = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        foldersPanel = new javax.swing.JPanel();

        setTitle("Import from Google Docs");
        setFocusTraversalPolicyProvider(true);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(600, 500));
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

        jSplitPane1.setDividerLocation(30);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        getListButton.setText("Get list");
        getListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getListButtonActionPerformed(evt);
            }
        });
        jSplitPane1.setTopComponent(getListButton);

        jSplitPane2.setDividerLocation(360);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setFocusCycleRoot(true);
        jSplitPane2.setPreferredSize(new java.awt.Dimension(300, 134));

        jPanel3.setMaximumSize(new java.awt.Dimension(130, 33));

        jPanel4.setLayout(new java.awt.GridBagLayout());

        openButton.setText("Open");
        openButton.setEnabled(false);
        openButton.setMaximumSize(new java.awt.Dimension(1000, 28));
        openButton.setPreferredSize(new java.awt.Dimension(119, 28));
        openButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(openButton, gridBagConstraints);

        openViaBrowserButton.setText("Open via Browser");
        openViaBrowserButton.setEnabled(false);
        openViaBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openViaBrowserButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(openViaBrowserButton, gridBagConstraints);

        downloadButton.setText("Download");
        downloadButton.setEnabled(false);
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(downloadButton, gridBagConstraints);

        openInBrowser.setText("Open in Browser");
        openInBrowser.setEnabled(false);
        openInBrowser.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        openInBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInBrowserActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(openInBrowser, gridBagConstraints);

        formatChoosePanel.setLayout(new java.awt.GridBagLayout());

        autoUpdate.setText("Autoupdate");
        autoUpdate.setToolTipText("Update after Save");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        formatChoosePanel.add(autoUpdate, gridBagConstraints);

        formatChooserLabel.setText("As");
        jPanel5.add(formatChooserLabel);

        formatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel5.add(formatComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        formatChoosePanel.add(jPanel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(formatChoosePanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel4.add(jSeparator1, gridBagConstraints);

        jPanel3.add(jPanel4);

        jSplitPane2.setRightComponent(jPanel3);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.BorderLayout());

        jLabel2.setText("Filter:");
        jLabel2.setEnabled(false);
        jPanel7.add(jLabel2, java.awt.BorderLayout.WEST);

        filterText.setEnabled(false);
        filterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterTextActionPerformed(evt);
            }
        });
        filterText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterTextKeyReleased(evt);
            }
        });
        jPanel7.add(filterText, java.awt.BorderLayout.CENTER);

        jPanel6.add(jPanel7, java.awt.BorderLayout.NORTH);

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

        jPanel6.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel6.add(foldersPanel, java.awt.BorderLayout.PAGE_END);

        jSplitPane2.setLeftComponent(jPanel6);

        jSplitPane1.setBottomComponent(jSplitPane2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        new OpenWrapper(WrapperFactory.getWrapperForCredentials(system),true).open();
    }//GEN-LAST:event_openButtonActionPerformed

    private void donwloadTextDocument(final Document entry, final Wrapper wrapper, final boolean openAfterDownload) throws MalformedURLException, IOException, URISyntaxException, UnsupportedEncodingException, HeadlessException {
        String directory = Configuration.getDirectoryToStoreFiles();
        if (directory==null) directory = Configuration.getWorkingPath();
        String documentUrl = null;
        String documentTitle = entry.getTitle();
        if (entry.getDocumentLink().indexOf("file")==-1) {
            documentTitle = createFileName(wrapper,entry);
        }
        if ("?".equals(directory)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(documentTitle));
            if (fileChooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
                documentUrl = fileChooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }
        } else {
            documentUrl = directory +File.separator+documentTitle;
        }
        documentUrl = Util.findAvailableFileName(documentUrl);
        final URI uri;
        if ("Google Docs".equals(wrapper.getSystem()) && entry.getId().indexOf("file")!=-1) {
            uri = new URI(entry.getDocumentLink());
                if (autoUpdate.isSelected()) {
                    Configuration.addToGlobalMapOfFiles(documentUrl,entry.getDocumentLink(),Util.getFormatForMimeType(entry.getContentType()));
                }
        } else {
            if (wrapper.downloadInGivenFormatSupported()) {
                uri = wrapper.getUriForEntry(entry,(OOoFormats)formatComboBox.getSelectedItem());
                if (autoUpdate.isSelected()) {
                    Configuration.addToGlobalMapOfFiles(documentUrl,entry.getDocumentLink(),(OOoFormats)formatComboBox.getSelectedItem());
                }
            } else {
                uri = wrapper.getUriForEntry(entry);
            }
        }
        downloadURI(documentUrl, uri, wrapper, openAfterDownload);
    }

    private String createFileName(final Wrapper wrapper,final Document entry) {
        String documentTitle = entry.getTitle().replaceAll("\\?", "").replaceAll("\\*", "").replace(File.separatorChar, '_').replace('/', '_');
        String fileExtension = "";
        if (wrapper.downloadInGivenFormatSupported() && entry.isConvertable()) {
             fileExtension = ((OOoFormats)formatComboBox.getSelectedItem()).getFileExtension();
        } else {
            boolean isDoc = entry.getId().indexOf("/document%3A") != -1;
            boolean isPresentation = entry.getId().indexOf("/presentation%3A") != -1;
            boolean isSpreadsheet = entry.getId().indexOf("/spreadsheet%3A") != -1;            
            if (isDoc) {
                fileExtension="odt";
            } else if (isPresentation) {
                fileExtension="ppt";
            } else if (isSpreadsheet) {
                fileExtension="ods";
            }
        }
        if (!documentTitle.toLowerCase().endsWith("."+fileExtension) && (fileExtension.length()>0)) {
                documentTitle += "."+fileExtension;
        }        
        return documentTitle;
    }

    
    private void downloadURI(final String documentUrl, final URI uri, final Wrapper wrapper, final boolean openAfterDownload) throws MalformedURLException, URISyntaxException {
        final Downloader downloader = wrapper.getDownloader(uri, 
            documentUrl);
        final Uploading progressWindow = new Uploading();
        progressWindow.setMessage(wrapper.getSystem()+" -> OpenOffice.org");
        progressWindow.setVisible(true);            
        progressWindow.showProgressBar();
        downloader.addIOListener(new ImportIOListener(documentUrl, progressWindow,openAfterDownload));
        downloader.start();
    }

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        try {
//            Desktop.getDesktop().browse(new URI("http://przemelek.googlepages.com/kontakt"));
            Util.openBrowserForURL(this, "http://www.przemelek.pl/kontakt");
        } catch (Exception e) {
            // OK, it's not crutial problem, so we ignore it ;-)'
        }
    }//GEN-LAST:event_jLabel1MouseClicked
        
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed
    
    private void setButtonsEnable(boolean openState, boolean openViaBrowserState, boolean openInBrowserState) {
        openButton.setEnabled(openState);
        openViaBrowserButton.setEnabled(openViaBrowserState);
        openInBrowser.setEnabled(openInBrowserState);
        downloadButton.setEnabled(openState);
    }
    
    private void getListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getListButtonActionPerformed
        getListButton.setEnabled(false);
        jTable1.setEnabled(false);
        jLabel2.setEnabled(false);
        filterText.setEnabled(false);
        final Cursor currentCursor = ImportDialog.this.getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setButtonsEnable(false, false, false);
        Util.startNewThread(Configuration.getClassLoader(), new Runnable() {
            public void run() {
                try {
                    final Wrapper wrapper = WrapperFactory.getWrapperForCredentials(system); 
                    wrapper.login(loginPanel1.getCreditionals());
                    List<Document> documents = wrapper.getListOfDocs(false);
                    fillListOfDocuments(wrapper,documents);
                    filterText.requestFocusInWindow();
                } catch (Exception e) {
                    ImportDialog.this.setCursor(currentCursor);
                    e.printStackTrace();            
                    Configuration.log(Configuration.getResources().getString("Problem:_")+e.getMessage());
                    Configuration.log(e);
                    JOptionPane.showMessageDialog(ImportDialog.this,Configuration.getResources().getString("Problem:_")+e.getMessage());
                }
                finally {
                    ImportDialog.this.setCursor(currentCursor);
                    ImportDialog.this.jTable1.setEnabled(true);
                    ImportDialog.this.filterText.setEnabled(true);
                    ImportDialog.this.getListButton.setEnabled(true);
                    ImportDialog.this.jLabel2.setEnabled(true);
                }                
                repaint();                
            }
        });
    }//GEN-LAST:event_getListButtonActionPerformed
    
    
    private void fillListOfDocuments(final Wrapper wrapper, List<Document> documents) {
        final DocumentsTableModel dtm = new DocumentsTableModel(wrapper);
        for (Document entry : documents) {
            dtm.add(entry);
        }
        jTable1.setModel(dtm);
        Map<String,String> folder2color = dtm.getFolders2ColorsMap();
        if (folder2color.size()>0) {
            foldersPanel.removeAll();
            foldersPanel.setVisible(true);
            foldersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            int idx=0;
            addFolderLabel("black","white","",dtm);
            for (Map.Entry<String,String> entry:folder2color.entrySet()) {
                String[] colors = entry.getValue().split("\\|");
                final String folderName = entry.getKey();
                addFolderLabel(colors[1],colors[0], folderName, dtm);
            }
        }
        jTable1.clearSelection();
        jLabel2.setEnabled(true);
        filterText.setEnabled(true);
        filterText.requestFocusInWindow();
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Document entry = ((DocumentsTableModel) jTable1.getModel()).getEntry(jTable1.getSelectedRow());
//                formatChoosePanel.setVisible(false);
                formatChoosePanel.setVisible(true);
                formatChooserLabel.setVisible(false);
                formatComboBox.setVisible(false);
                openViaBrowserButton.setEnabled(false);
                openInBrowser.setEnabled(false);
                boolean enabled = false;
                if (wrapper.downloadInGivenFormatSupported()) {
                    List<OOoFormats> list = wrapper.getListOfSupportedForDownloadFormatsForEntry(entry);
                    if (list.size() > 0) {
//                        formatChoosePanel.setVisible(true);
                        enabled = true;
                        formatChooserLabel.setVisible(true);
                        formatComboBox.setVisible(true);
                        formatComboBox.setModel(new DefaultComboBoxModel(list.toArray()));
                        openViaBrowserButton.setEnabled(true);
                        openInBrowser.setEnabled(true);
                    }
                }
                if (entry != null) {
                    ImportDialog.this.setButtonsEnable(true, enabled, enabled);
                } else {
                    ImportDialog.this.setButtonsEnable(false, false, false);
                }
            }
        });
    }
    
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

private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        new OpenWrapper(WrapperFactory.getWrapperForCredentials(system),false).open();
}//GEN-LAST:event_downloadButtonActionPerformed

private void filterTextKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterTextKeyReleased
    final int keyCode = evt.getKeyCode();
    if (keyCode!=KeyEvent.VK_DOWN && keyCode!=KeyEvent.VK_UP && keyCode!=KeyEvent.VK_ENTER) {
        String text = filterText.getText();
        DocumentsTableModel model = (DocumentsTableModel)jTable1.getModel();
        model.setFilter(text.toUpperCase());
        model.fireTableDataChanged();
    }
}//GEN-LAST:event_filterTextKeyReleased

private void filterTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterTextActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_filterTextActionPerformed
    
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        new ImportDialog(null, true, ".", "Google Docs", null).setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoUpdate;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton downloadButton;
    private javax.swing.JTextField filterText;
    private javax.swing.JPanel foldersPanel;
    private javax.swing.JPanel formatChoosePanel;
    private javax.swing.JLabel formatChooserLabel;
    private javax.swing.JComboBox formatComboBox;
    private javax.swing.JButton getListButton;
    private org.openoffice.gdocs.util.GoogleDocsWrapper googleDocsWrapper1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTable jTable1;
    private org.openoffice.gdocs.ui.LoginPanel loginPanel1;
    private javax.swing.JButton openButton;
    private javax.swing.JButton openInBrowser;
    private javax.swing.JButton openViaBrowserButton;
    // End of variables declaration//GEN-END:variables

}

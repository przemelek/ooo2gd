// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.ui.LoginPanel;
import org.openoffice.gdocs.ui.models.MyCellRenderer;
import org.openoffice.gdocs.ui.models.MyEditorRenderer;
import org.openoffice.gdocs.util.Creditionals;
import org.openoffice.gdocs.util.Document;
import org.openoffice.gdocs.util.Util;
import org.openoffice.gdocs.util.Wrapper;
import org.openoffice.gdocs.util.WrapperFactory;

import com.sun.star.frame.XFrame;

/**
 *
 * @author  rmk
 */
public class UploadDialog extends javax.swing.JFrame {
                           
    private String pathName;
    private boolean getNameFromComboBox = false;
    private XFrame xFrame;
    private String system;  
    private Wrapper wrapper;
    private boolean isUpdate;
    public UploadDialog(String pathName,String system,XFrame frame) {
        super();
        xFrame = frame;
        this.pathName = pathName;
        this.system = system;
        wrapper = WrapperFactory.getWrapperForCredentials(system);
        initComponents();
        docNameLabel2.setVisible(false);
        docNameComboBox.setVisible(false);
        refreshButton.setVisible(false);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/openoffice/gdocs/resources/refresh.png");
        byte[] buf = new byte[1024*10];
        try {
            int size = is.read(buf);                        
            refreshButton.setIcon(new ImageIcon(java.util.Arrays.copyOf(buf, size),"Refresh list"));
            refreshButton.setToolTipText("Refresh list");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        pack();
        loginPanel1.setSystem(system);
        String docName = new File(pathName).getName();            
        setVisibleForDocName(true);            
        setDocumentTitle(docName);
        docNameLabel1.setText(Configuration.getResources().getString("Document_name:"));
        docNameLabel2.setText(Configuration.getResources().getString("Document_name:"));
        setTitle(Configuration.getStringFromResources("Export_to_Google_Docs", system));        
        setServerLineVisible(false);
        if (wrapper.isServerSelectionNeeded()) {
            setServerLineVisible(true);
            for (String str:wrapper.getListOfServersForSelection()) {
                serversComboBox.addItem(str);                
            }
        }
        if (wrapper.updateSupported()) {
            refreshListOfDocumentsInDocNameComboBox(wrapper,true);
        }
        toFront();
    }
    
    private void refreshListOfDocumentsInDocNameComboBox(Wrapper wrapper,boolean useExistinListIfPossible) {
            boolean hasList = false;
            docNameComboBox.removeAllItems();
            List<Document> docsList = null;
            try {
                docsList = wrapper.getListOfDocs(useExistinListIfPossible);
                hasList = true;
            } catch(Exception e) {
                // we left empty... Update is impossible, because we cannot get list of documents :-(
                Configuration.log("Cannot get list of documents :-(");
                Configuration.log(e);
            }
            if (!hasList) {
                try {
                    wrapper.login(loginPanel1.getCreditionals());
                    docsList = wrapper.getListOfDocs(useExistinListIfPossible);
                    hasList=true;
                } catch(Exception e) {
                    // we left empty... Update is impossible, because we cannot get list of documents :-(
                    Configuration.log("Cannot get list of documents :-(");
                    Configuration.log(e);
                }                    
            }
            if (hasList && docsList!=null) {
                docNameComboBox.setRenderer(new MyCellRenderer());                                
                docNameComboBox.setEditor(new MyEditorRenderer());                 
                docNameLabel1.setVisible(false);
                docName.setVisible(false);
                docNameLabel2.setVisible(true);
                docNameComboBox.setVisible(true);
                refreshButton.setVisible(true);
                docNameComboBox.addItem(docName.getText());
                Map<String,Document> map = new HashMap<String,Document>();
                for (Document doc:docsList) {
                    docNameComboBox.addItem(doc);
                    map.put(doc.getId(),doc);
                }
                getNameFromComboBox=true;
            }
    }
    
    public void setServerLineVisible(boolean visibility) {
        serverLabel.setVisible(visibility);
        serverConfiguration.setVisible(visibility);
        serversComboBox.setVisible(visibility);
        loginPanel1.setVisible(!visibility);
        docName.setPreferredSize(docName.getSize());
        pack();
    }
    
    public void setVisibleForDocName(boolean visible) {
        documentNamePanel.setVisible(visible);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        documentNamePanel = new javax.swing.JPanel();
        docName = new javax.swing.JTextField();
        docNameLabel1 = new javax.swing.JLabel();
        serversComboBox = new javax.swing.JComboBox();
        serverLabel = new javax.swing.JLabel();
        serverConfiguration = new javax.swing.JButton();
        docNameLabel2 = new javax.swing.JLabel();
        docNameComboBox = new javax.swing.JComboBox();
        refreshButton = new javax.swing.JButton();
        loginPanel1 = new org.openoffice.gdocs.ui.LoginPanel();
        jLabel1 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export to Google Docs");
        setLocationByPlatform(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        docNameLabel1.setText("Document name:");

        serversComboBox.setEditable(true);

        serverLabel.setText("Server:");

        serverConfiguration.setText("?");
        serverConfiguration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverConfigurationActionPerformed(evt);
            }
        });

        docNameLabel2.setText("Document name:");

        docNameComboBox.setEditable(true);

        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout documentNamePanelLayout = new javax.swing.GroupLayout(documentNamePanel);
        documentNamePanel.setLayout(documentNamePanelLayout);
        documentNamePanelLayout.setHorizontalGroup(
            documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(documentNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(documentNamePanelLayout.createSequentialGroup()
                        .addGroup(documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(docNameLabel1)
                            .addComponent(serverLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(documentNamePanelLayout.createSequentialGroup()
                                .addComponent(serversComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(docName)))
                    .addGroup(documentNamePanelLayout.createSequentialGroup()
                        .addComponent(docNameLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(docNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        documentNamePanelLayout.setVerticalGroup(
            documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(documentNamePanelLayout.createSequentialGroup()
                .addGroup(documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serversComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverLabel)
                    .addComponent(serverConfiguration))
                .addGap(12, 12, 12)
                .addGroup(documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(docNameLabel1)
                    .addComponent(docName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(documentNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(docNameLabel2)
                    .addComponent(docNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        jLabel1.setText("<html><font size=\"1\">(c) <u><font color=\"blue\">Przemyslaw Rumik</font></u></font></html>");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loginPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(156, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addGap(47, 47, 47)
                        .addComponent(cancelButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)
                        .addGap(58, 58, 58)))
                .addGap(121, 121, 121))
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(documentNamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(loginPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(documentNamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.toFront();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        this.toFront();
    }//GEN-LAST:event_formWindowActivated

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        this.toFront();
    }//GEN-LAST:event_formComponentShown

private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
try {
            Desktop.getDesktop().browse(new URI("http://przemelek.googlepages.com/kontakt"));
        } catch (Exception e) {
            // OK, it's not crutial problem, so we ignore it ;-)'
        }
}//GEN-LAST:event_jLabel1MouseClicked

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        upload=true;
        this.setVisible(false);
        final Uploading uploading = new Uploading();
        Util.startNewThread(Configuration.getClassLoader(), new Runnable() {
            public void run() {
                try {
                    Creditionals credentionals;                    
                    if (wrapper.isServerSelectionNeeded()) {
                        String serverPath = (String)serversComboBox.getEditor().getItem();
                        wrapper.setServerPath(serverPath);
                        credentionals = wrapper.getCreditionalsForServer(serverPath);
                    } else {
                        credentionals = loginPanel1.getCreditionals();
                    }
                    String docName=getDocumentTitle();
                    
                    boolean updateInsteadOfCreatingNew = false;
                    
                    if (getNameFromComboBox) {
                        Object obj = docNameComboBox.getEditor().getItem();
                        if (obj instanceof String) {
                            docName = (String)obj;
                        } else {
                            docName = ((Document)obj).getTitle();
                            updateInsteadOfCreatingNew = true;
                            ///((Document)obj).getDocumentLink()
                        }
                    }
                    
                    uploading.setVisible(true);
                    
                    if (credentionals.isEmpty() && wrapper.checkIfAuthorizationNeeded(pathName, docName)) {
                        uploading.setVisible(false);
                        // we need ask user for credentials :-)
                        org.openoffice.gdocs.ui.LoginPanel loginPanel = new LoginPanel() {

                            @Override
                            public void storeCredentials(Creditionals creditionals) {
                                wrapper.storeCredentials(creditionals);
                            }
                            
                        };
                        loginPanel.setSystem(system);
 
                        final MyLoginDialog dialog = new MyLoginDialog(UploadDialog.this);
                        dialog.setTitle("User & Password");
                        JPanel buttonsPanel = new JPanel();
                        JButton okButton = new JButton("OK");
                        okButton.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                dialog.handleOK();
                            }
                        });
                        JButton cancelButton = new JButton("Cancel");
                        cancelButton.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                dialog.handleOK();
                            }
                        });
                        buttonsPanel.add(okButton);
                        buttonsPanel.add(cancelButton);
                        dialog.getContentPane().setLayout(new BorderLayout());
                        dialog.getContentPane().add(loginPanel,BorderLayout.NORTH);
                        dialog.getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
                        dialog.setModal(true);
                        dialog.pack();
                        dialog.setVisible(true);
                        dialog.setVisible(false);
                        if (dialog.getReturnCode()==JOptionPane.OK_OPTION) {
                            credentionals = loginPanel.getCreditionals();
                        } else {
                            upload = false;
                        }

                        uploading.setVisible(true);
                    }
                    if (credentionals.isEmpty() && wrapper.isServerSelectionNeeded()) {
                        wrapper.storeCredentials(credentionals);
                    }
                    
                    uploading.setVisible(true);
                    wrapper.login(credentionals);
                    boolean upload = true;
                    // File to store is OpenOffice Impress Presentation?
                    if (needConversion(pathName,system)) {
                        if (pathName.toLowerCase().indexOf(".odp")!=-1) {                        
                            upload = convertPresentation(upload);
                        }
                    }
                    if (upload) {
                        boolean success = false;
                        if (updateInsteadOfCreatingNew) {
                            Document docToUpdate = ((Document)docNameComboBox.getSelectedItem());
                            success = wrapper.update(pathName, docToUpdate.getDocumentLink());
                        } else {
                            success = wrapper.upload(pathName,docName);
                        }
                        if (success) {
                            String successMsg = "File Uploaded";
                            if (updateInsteadOfCreatingNew) {                            
                                successMsg = "File Updated";
                            } 
                            JOptionPane.showMessageDialog(null, successMsg);
                        } else {
                            JOptionPane.showMessageDialog(null, "Cannot upload document "+pathName,"Problem",JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Configuration.log(e);
                    JOptionPane.showMessageDialog(null,"Problem: "+e.getMessage());                    
                }
                finally {
                    uploading.setVisible(false);
                }        
            }

			private boolean convertPresentation(boolean upload) {
				String msg = Configuration.getStringFromResources("NEED_CONVERT_PPT",system);
				int option = JOptionPane.showConfirmDialog(null,msg,"ODP -> PPT",JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
				    String filterName = "MS PowerPoint 97";
				    try {
				        pathName=Util.convertDocumentToFormat(pathName, filterName, "ppt", xFrame);
				    } catch (Exception e) {
                                        Configuration.log(e);
				        JOptionPane.showMessageDialog(null,"Sorry, OOo2GD wasn't able to convert this document.\nTry to do it using Save As.. option in File Menu,\nremember Save your document as Microsoft PowerPoint 97 (PPT).");
				    }
				} else {
				    upload = false;
				}
				return upload;
			}

            private boolean needConversion(String pathName, String system) {
                Wrapper wrapper = WrapperFactory.getWrapperForCredentials(system);
                return wrapper.neeedConversion(pathName);
            }
        });
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    upload=false;
    this.setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void serverConfigurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverConfigurationActionPerformed
    try {
        new ServersManager(wrapper).setVisible(true);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getLocalizedMessage());
    }
}//GEN-LAST:event_serverConfigurationActionPerformed

private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
    refreshListOfDocumentsInDocNameComboBox(WrapperFactory.getWrapperForCredentials(system),false);
}//GEN-LAST:event_refreshButtonActionPerformed

    
    public boolean getUpload() {
        return upload;
    }
    
    public void setDocumentTitle(String title) {
        docName.setText(title);
    }
    
    public String getDocumentTitle() {
        return docName.getText();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField docName;
    private javax.swing.JComboBox docNameComboBox;
    private javax.swing.JLabel docNameLabel1;
    private javax.swing.JLabel docNameLabel2;
    private javax.swing.JPanel documentNamePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private org.openoffice.gdocs.ui.LoginPanel loginPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton serverConfiguration;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JComboBox serversComboBox;
    // End of variables declaration//GEN-END:variables
    private boolean upload = false;
    
    public static void main(String... args) {
        UploadDialog dialog = new UploadDialog("toster.odt", "Google Docs", null);
        dialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dialog.setVisible(true);        
    }
    
}

// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.ui.dialogs;

import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.util.AuthenticationException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import org.openoffice.gdocs.util.GoogleDocsWrapper;
import org.openoffice.gdocs.ui.*;
import org.openoffice.gdocs.ui.models.DocumentsTableModel;

/**
 *
 * @author  rmk
 */
public class ImportDialog extends JDialog {
    
    /** Creates new form ImportDialog */
    public ImportDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        jTable1.setModel(new DocumentsTableModel());
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        openButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setFocusTraversalPolicyProvider(true);
        setLocationByPlatform(true);
        setTitle("Import from Google Docs");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.add(loginPanel1);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel2.add(closeButton, new java.awt.GridBagConstraints());

        jLabel1.setText("<html>(c) <u><font color=\"blue\">Przemyslaw Rumik</font></u></html>");
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

        add(jPanel2, java.awt.BorderLayout.SOUTH);

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
        openButton.setText("Open");
        openButton.setEnabled(false);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        jPanel3.add(openButton);

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

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        try {
            Desktop.getDesktop().browse(new URI("http://przemelek.googlepages.com/kontakt"));
        } catch (Exception e) {
            // OK, it's not crutial problem, so we ignore it ;-)'
        }
    }//GEN-LAST:event_jLabel1MouseClicked
    
    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        try {
            GoogleDocsWrapper wrapper = new GoogleDocsWrapper();
            wrapper.login(loginPanel1.getCreditionals());
            DocumentElement entry = new DocumentElement(((DocumentsTableModel)jTable1.getModel()).getEntry(jTable1.getSelectedRow()));
            Desktop.getDesktop().browse( getUriForEntry(entry) );
        } catch (AuthenticationException e) {
            JOptionPane.showMessageDialog(this,"Invalid Creditionals.");
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(this,e.getMessage());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,"Cannot open document in default browser");
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private URI getUriForEntry(final DocumentElement entry) throws URISyntaxException {
        String id = entry.getId().split("%3A")[1];
        String type = entry.getId().split("%3A")[0];            
        String uriStr = "";
        if ("document".equals(type)) {
            uriStr = "http://docs.google.com/MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=oo";
        } else if ("spreadsheet".equals(type)) {
            uriStr = "http://spreadsheets.google.com/fm?id="+id+"&hl=en&fmcmd=13";
        } else if ("presentation".equals(type)) {
            uriStr = "http://docs.google.com/MiscCommands?command=saveasdoc&docID="+id+"&exportFormat=ppt";
        }
        return new URI(uriStr);
    }
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed
    
    private class DocumentElement {
        private DocumentListEntry entry;
        public DocumentElement(DocumentListEntry entry) {
            this.entry = entry;
        }
        public String getDocumentLink() {
            return entry.getDocumentLink().getHref();
        }
        public String getId() {
            return entry.getId();
        }
        public String toString() {
            return entry.getTitle().getPlainText();
        }
    }
    
    private void getListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getListButtonActionPerformed
        GoogleDocsWrapper wrapper = new GoogleDocsWrapper();
        try {
            wrapper.login(loginPanel1.getCreditionals());
            jTable1.setEnabled(true);
            openButton.setEnabled(true);
            List<DocumentElement> list = new ArrayList<DocumentElement>();
            DocumentsTableModel dtm = new DocumentsTableModel();        
            for (DocumentListEntry entry:wrapper.getListOfDocs()) {
                if ( entry.getId().startsWith("document") ) {
                    dtm.add(entry);
                }
            }
            jTable1.setModel(dtm);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Problem: "+e.getMessage());
        }

        this.repaint();
    }//GEN-LAST:event_getListButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ImportDialog(new java.awt.Frame(), true).setVisible(true);
            }
        });
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton getListButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTable jTable1;
    private org.openoffice.gdocs.ui.LoginPanel loginPanel1;
    private javax.swing.JButton openButton;
    // End of variables declaration//GEN-END:variables

}

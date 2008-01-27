package org.openoffice.gdocs;

import java.awt.HeadlessException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.ui.dialogs.ImportDialog;
import org.openoffice.gdocs.ui.dialogs.UploadDialog;

public abstract class AbstractGDocs {
  
  protected abstract boolean isModified();
  protected abstract boolean storeToDisk();
  protected abstract String getCurrentDocumentPath();
  
  protected void exportToGoogleDocs() {
    final String path = getCurrentDocumentPath();
    startNewThread(new Runnable() {
      public void run() {
        if (path != null && !path.equals("")) {
          try {
            URL url = new URL(path);
            File file = new File(url.toURI());
            if (file.isFile()) {
              boolean doUpload = true;
              if (isModified()) {
                String notSavedMessage = Configuration.getResources()
                    .getString("Your_file_was_modified");
                int option = JOptionPane.showConfirmDialog(null,
                    notSavedMessage);
                if (option == JOptionPane.YES_OPTION) {
                  if (!storeToDisk()) {
                    doUpload = false;
                    JOptionPane.showMessageDialog(null, Configuration
                        .getResources().getString(
                            "Cannot_save_file_on_disk...."));
                  }
                } else if (option == JOptionPane.CANCEL_OPTION) {
                  doUpload = false;
                }
              }
              if (doUpload) {
                String pathName = file.getPath();
                new UploadDialog(pathName).setVisible(true);
              }
            } else {
              JOptionPane.showMessageDialog(null, Configuration.getResources()
                  .getString(
                      "Sorry..._you_must_first_save_your_file_on_hard_disk."));
            }
          } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Configuration.getResources()
                .getString("Problem:_")
                + e.getMessage());
          }
        } else {
          JOptionPane.showMessageDialog(null,
              Configuration.getResources().getString(
                  "Sorry..._you_must_first_save_your_file_on_hard_disk."));
        }
      }
    });
  }

  protected void importFromGoogleDocs() throws HeadlessException {
    startNewThread(new Runnable() {
      public void run() {
        try {
          new ImportDialog(null, true, getCurrentDocumentPath()).setVisible(true);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
        }
      }
    });
  }
  
  private void startNewThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.setContextClassLoader(this.getClass().getClassLoader());
    thread.start();        
  }
  
  public static String getDocumentDirectory() {
    String userHome = System.getProperty("user.home");
    File f = new File(userHome, "ooo2gdtest/");
    if(!f.exists()){
      f.mkdirs();
    }
    try {
      return f.toURI().toURL().toExternalForm();
    } catch (MalformedURLException e) {
      throw new InternalError("Can not convert URI ["+f.toURI()+"] to an URL");
    }
  }
}

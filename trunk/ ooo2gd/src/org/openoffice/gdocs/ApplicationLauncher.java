package org.openoffice.gdocs;

import java.awt.HeadlessException;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.SwingUtilities;

public class ApplicationLauncher extends AbstractGDocs {
  protected boolean isModified() throws HeadlessException {
    return false;
  }
  
  protected boolean storeToDisk() {
    return true;
  }
  
  protected String getCurrentDocumentPath() {
    return getDocumentDirectory();
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

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new ApplicationLauncher().importFromGoogleDocs();
      }
    });
  }
}

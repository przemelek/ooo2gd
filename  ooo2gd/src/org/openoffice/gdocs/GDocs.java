// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs;

import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.util.XModifiable;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOError;
import java.net.URL;
import javax.swing.JOptionPane;
import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.ui.dialogs.ConfigDialog;
import org.openoffice.gdocs.ui.dialogs.ImportDialog;
import org.openoffice.gdocs.ui.dialogs.UploadDialog;

public final class GDocs extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              com.sun.star.frame.XDispatchProvider,
              com.sun.star.lang.XInitialization,
              com.sun.star.frame.XDispatch
{
    private static final String GDOCS_PROTOCOL = "org.openoffice.gdocs.gdocs:";
    private static final String EXPORT_TO = "Export to Google Docs";
    private static final String IMPORT_FROM = "Import from Google Docs";
    private static final String CONFIGURE = "Configure";
    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = GDocs.class.getName();
    private static final String[] m_serviceNames = {"com.sun.star.frame.ProtocolHandler" };

    public GDocs( XComponentContext context )
    {
        m_xContext = context;
    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(GDocs.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch( com.sun.star.util.URL aURL,
                                                       String sTargetFrameName,
                                                       int iSearchFlags )
    {
        if ( aURL.Protocol.compareTo(GDOCS_PROTOCOL) == 0 )
        {
            if ( aURL.Path.compareTo(EXPORT_TO) == 0 ) {
                String path = getCurrentDocumentPath();
                XDispatch result = null;
                if (path!=null) {
                    result = this;
                }
                return result;
            } else if ( aURL.Path.compareTo(IMPORT_FROM) == 0 ) {
                return this;
            } else if ( aURL.Path.compareTo(CONFIGURE) == 0 ) {
                return this;
            }
        }
        return null;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch[] queryDispatches(
         com.sun.star.frame.DispatchDescriptor[] seqDescriptors )
    {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
            new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for( int i=0; i < nCount; ++i )
        {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                                             seqDescriptors[i].FrameName,
                                             seqDescriptors[i].SearchFlags );
        }
        return seqDispatcher;
    }

    // com.sun.star.lang.XInitialization:
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception
    {
        if ( object.length > 0 )
        {
        m_xFrame = (com.sun.star.frame.XFrame)UnoRuntime.queryInterface(
                com.sun.star.frame.XFrame.class, object[0]);
        }
    }

    // com.sun.star.frame.XDispatch:
     public void dispatch( com.sun.star.util.URL aURL,
                           com.sun.star.beans.PropertyValue[] aArguments )
    {
         if ( aURL.Protocol.compareTo(GDOCS_PROTOCOL) == 0 )
        {
            if ( aURL.Path.compareTo(EXPORT_TO) == 0 )
            {
                exportToGoogleDocs();
                return;
            }
            if ( aURL.Path.compareTo(IMPORT_FROM) == 0 )
            {
                importFromGoogleDocs();
                return;
            }
            if ( aURL.Path.compareTo(CONFIGURE) == 0 )
            {
                startNewThread(new Runnable() {
                    public void run() {
                        try {
                            new ConfigDialog(null,true).setVisible(true);
                        } catch (Exception e) {
                            
                        }
                    }
                });
                return;
            }
            
        }
    }

    private void exportToGoogleDocs() {
        final String path = getCurrentDocumentPath();
        startNewThread(new Runnable() {
            public void run() {                
                if (path!=null && !path.equals("")) {
                    try {
                        URL url = new URL(path);
                        File file = new File(url.toURI());
                        if (file.isFile()) {
                            boolean doUpload = true;
                            if (isModified()) {
                                String notSavedMessage = Configuration.getResources().getString("Your_file_was_modified");
                                int option = JOptionPane.showConfirmDialog(null,notSavedMessage);
                                if (option == JOptionPane.YES_OPTION) {
                                        if ( !storeToDisk() ) {
                                            doUpload = false;
                                            JOptionPane.showMessageDialog(null,Configuration.getResources().getString("Cannot_save_file_on_disk...."));
                                        }
                                } else if  (option == JOptionPane.CANCEL_OPTION) {
                                    doUpload = false;
                                }
                            }
                            if (doUpload) {
                                String pathName=file.getPath();
                                new UploadDialog(pathName).setVisible(true);
                            }
                        } else {                            
                            JOptionPane.showMessageDialog(null,Configuration.getResources().getString("Sorry..._you_must_first_save_your_file_on_hard_disk."));
                        }
                      } catch (Exception e) {
                            JOptionPane.showMessageDialog(null,Configuration.getResources().getString("Problem:_")+e.getMessage());
                      }
                  } else {
                      JOptionPane.showMessageDialog(null,Configuration.getResources().getString("Sorry..._you_must_first_save_your_file_on_hard_disk."));
                  }
            }
        });
    }

    private void importFromGoogleDocs() throws HeadlessException {
        startNewThread(new Runnable() {
            public void run() {
                try {
                    new ImportDialog(null,true, getTempPath()).setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,e.getMessage());
                }
            }
        });
    }

    private boolean storeToDisk() {
        // OK, here we will have small sample of wrong behavior, this means exception driven control flow... but it's easier ;p
        boolean result = true;
        XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, m_xFrame.getController().getModel()) ;        
        try {
            storable.store();
        } catch (IOException ioe) {
            result = false;
        }
        return result;
    }
    
    private void startNewThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setContextClassLoader(this.getClass().getClassLoader());
        thread.start();        
    }
    
    private String getCurrentDocumentPath() {
        XModel xDoc = (XModel) UnoRuntime.queryInterface(
        XModel.class, m_xFrame.getController().getModel());
        return xDoc.getURL();
    }    

    private String getTempPath() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return tmpDir;
    }
    
    private boolean isModified() throws HeadlessException {
        XModifiable xModifable = (XModifiable) UnoRuntime.queryInterface(
        XModifiable.class, m_xFrame.getController().getModel());
        return xModifable.isModified();
    }
    
    public void addStatusListener( com.sun.star.frame.XStatusListener xControl,
                                    com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

    public void removeStatusListener( com.sun.star.frame.XStatusListener xControl,
                                       com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

}

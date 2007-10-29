package org.openoffice.gdocs;

import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import java.awt.HeadlessException;
import java.io.File;
import java.net.URL;
import javax.swing.JOptionPane;
import org.openoffice.gdocs.ui.ImportDialog;
import org.openoffice.gdocs.ui.UploadDialog;

public final class GDocs extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              com.sun.star.frame.XDispatchProvider,
              com.sun.star.lang.XInitialization,
              com.sun.star.frame.XDispatch
{
    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = GDocs.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.frame.ProtocolHandler" };


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
        if ( aURL.Protocol.compareTo("org.openoffice.gdocs.gdocs:") == 0 )
        {
            if ( aURL.Path.compareTo("Export to Google Docs") == 0 )
                return this;
            if ( aURL.Path.compareTo("Import from Google Docs") == 0 )
                return this;
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
         if ( aURL.Protocol.compareTo("org.openoffice.gdocs.gdocs:") == 0 )
        {
            if ( aURL.Path.compareTo("Export to Google Docs") == 0 )
            {
                exportToGoogleDocs();
                return;
            }
            if ( aURL.Path.compareTo("Import from Google Docs") == 0 )
            {
                importFromGoogleDocs();
                return;
            }
        }
    }

    private void exportToGoogleDocs() {
        XModel xDoc = (XModel) UnoRuntime.queryInterface(
        XModel.class, m_xFrame.getController().getModel());
        final String path = xDoc.getURL();
        if ((path!=null) && (!"".equals(path))) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                      try {
                        URL url = new URL(path);                                                                                
                        File file = new File(url.toURI());
                        if (file.isFile()) {
                            String pathName=file.getPath();                    
                            new UploadDialog(pathName).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(null,"Sorry... you must first save your file on hard disk.");
                        }
                      } catch (Exception e) {

                      }
                    }
                });
                thread.setContextClassLoader(this.getClass().getClassLoader());
                thread.start();
            }                         
    }

    private void importFromGoogleDocs() throws HeadlessException {
        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    new ImportDialog(null,true).setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,e.getMessage());
                }
            }
        });
        thread.setContextClassLoader(this.getClass().getClassLoader());
        thread.start();
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

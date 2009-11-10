package org.openoffice.gdocs.util;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import java.awt.Component;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.openoffice.gdocs.configuration.Configuration;

public class Util {
        public static String xorString(String input,String key) {
            char[] keyChars = key.toCharArray();
            char[] inputChars = input.toCharArray();
            for (int i=0; i<inputChars.length; i++) {
                inputChars[i]^=keyChars[i%keyChars.length];
            }
            return new String(inputChars);
        }
    
        public static String fileNameToOOoURL(final String fName) {
            StringBuilder sLoadUrl = new StringBuilder("file:///");
            sLoadUrl.append(fName.replace('\\', '/').replace("#", "%23"));
            return sLoadUrl.toString();
        }
    
        public static String convertDocumentToFormat(String pathName, String filterName, String newExt,XFrame xFrame ) throws com.sun.star.io.IOException, com.sun.star.lang.IllegalArgumentException,IOException {
            String fName = Util.fileNameToOOoURL(pathName);
            String newPathName = pathName.substring(0,pathName.lastIndexOf("."))+"."+newExt;
            String newFName = Util.fileNameToOOoURL(newPathName);

            PropertyValue[] properties = new PropertyValue[3];
            PropertyValue prop = new PropertyValue();
            prop.Name="Hidden";
            prop.Value=true;
            properties[0]=prop;
            properties[1]=new PropertyValue();
            properties[1].Name="ReadOnly";
            properties[1].Value=Boolean.valueOf(true);
            properties[2]=new PropertyValue();
            properties[2].Value="AsTemplate";
            properties[2].Value=Boolean.valueOf(true);
            XComponentLoader loader = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class,xFrame);
            XComponent xComp  = null;
            xComp = loader.loadComponentFromURL(fName, "_blank", 0, properties);

            XTextDocument textDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,xComp);
            XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, xComp) ;
            /*XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, xFrame.getController().getModel()) ; */
            PropertyValue[] propertyValue = new PropertyValue[ 3 ];
            propertyValue[0] = new com.sun.star.beans.PropertyValue();
            propertyValue[0].Name = "Overwrite";
            propertyValue[0].Value = Boolean.valueOf(true);
            propertyValue[1] = new com.sun.star.beans.PropertyValue();
            propertyValue[1].Name = "FilterName";
            propertyValue[1].Value = filterName;
            propertyValue[2]=new PropertyValue();
            propertyValue[2].Value="AsTemplate";
            propertyValue[2].Value=Boolean.valueOf(true);            
            //XOutputStreamToByteArrayAdapter outputStream = new XOutputStreamToByteArrayAdapter(); 
            //propertyValue[2] = new com.sun.star.beans.PropertyValue();
            //propertyValue[2].Name = "OutputStream";
            //propertyValue[2].Value = outputStream;
            try {
                storable.storeToURL( newFName, propertyValue );
            } catch (ErrorCodeIOException ecie) {
                System.out.println(ecie.ErrCode);
            }
//            storable.storeToURL( "private:stream", propertyValue );
//            outputStream.closeOutput();
//            FileOutputStream fos = new FileOutputStream(newPathName);
//            fos.write(outputStream.getBuffer());
//            fos.close();
            //xComp.dispose();
            return newPathName;
        }

        public static String convertDocumentToFormat(String pathName, OOoFormats sourceFormat, OOoFormats destinationFormats,XFrame xFrame ) throws com.sun.star.io.IOException, com.sun.star.lang.IllegalArgumentException,IOException {
            String fName = Util.fileNameToOOoURL(pathName);
            String newPathName = pathName.substring(0,pathName.lastIndexOf("."))+"."+destinationFormats.getFileExtension().toLowerCase();
            String newFName = Util.fileNameToOOoURL(newPathName);

            PropertyValue[] properties = new PropertyValue[3];
            PropertyValue prop = new PropertyValue();
            prop.Name="Hidden";
            prop.Value=true;
            properties[0]=prop;
            properties[1]=new PropertyValue();
            properties[1].Name="ReadOnly";
            properties[1].Value=Boolean.valueOf(true);
            properties[2]=new PropertyValue();
            properties[2].Value="AsTemplate";
            properties[2].Value=Boolean.valueOf(true);
            XComponentLoader loader = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class,xFrame);
            XComponent xComp  = null;
            xComp = loader.loadComponentFromURL(fName, "_blank", 0, properties);

            XTextDocument textDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,xComp);
            XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, xComp) ;
            /*XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, xFrame.getController().getModel()) ; */
            PropertyValue[] propertyValue = new PropertyValue[ 3 ];
            propertyValue[0] = new com.sun.star.beans.PropertyValue();
            propertyValue[0].Name = "Overwrite";
            propertyValue[0].Value = Boolean.valueOf(true);
            propertyValue[1] = new com.sun.star.beans.PropertyValue();
            propertyValue[1].Name = "FilterName";
            propertyValue[1].Value = destinationFormats.getFilterName();
            propertyValue[2]=new PropertyValue();
            propertyValue[2].Value="AsTemplate";
            propertyValue[2].Value=Boolean.valueOf(true);            
            //XOutputStreamToByteArrayAdapter outputStream = new XOutputStreamToByteArrayAdapter(); 
            //propertyValue[2] = new com.sun.star.beans.PropertyValue();
            //propertyValue[2].Name = "OutputStream";
            //propertyValue[2].Value = outputStream;
            try {
                storable.storeToURL( newFName, propertyValue );
            } catch (ErrorCodeIOException ecie) {
                System.out.println(ecie.ErrCode);
            }
//            storable.storeToURL( "private:stream", propertyValue );
//            outputStream.closeOutput();
//            FileOutputStream fos = new FileOutputStream(newPathName);
//            fos.write(outputStream.getBuffer());
//            fos.close();
            //xComp.dispose();
            return newPathName;
        }

        
        public static void openInOpenOffice(Component parent,final String fName, XFrame xFrame) throws com.sun.star.lang.IllegalArgumentException, com.sun.star.io.IOException {
            if (!Configuration.isUseExec()) {
                String sLoadUrl = Util.fileNameToOOoURL(fName);
                XComponentLoader loader = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class,xFrame);
                XComponent xComp = loader.loadComponentFromURL(sLoadUrl, "_blank", 0, new PropertyValue[0]);
                XTextDocument aTextDocument = (XTextDocument)UnoRuntime.queryInterface(com.sun.star.text.XTextDocument.class, xComp);
            } else {
                // bad luck, we need to use direct method to run OO.org :-(
                String cmd[] = {"open", Configuration.getPathForOOoExec(parent) , fName};
                try {
                    Runtime.getRuntime().exec(cmd);
                    Configuration.store();
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(parent, "Problem: "+ioe.getMessage(),"Problem",JOptionPane.ERROR_MESSAGE);                    
                }
            }
        }
        
            
        public static void startNewThread(ClassLoader classLoader, Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setContextClassLoader(classLoader);
            thread.start();        
        }
        
        public String getTempPath() {
            String tmpDir = System.getProperty("java.io.tmpdir");
            return tmpDir;
        }
        
        public static String findAvailableFileName(String destFileURI) {
            String destFileName = destFileURI.substring(0,destFileURI.lastIndexOf("."));
            String destFileExt = destFileURI.substring(destFileURI.lastIndexOf(".")+1);
            int count = 1;      
            File f;
            while ((f=new File(destFileURI)).exists() && !Configuration.getOverwritteFlag()) {
                destFileURI=destFileName+"("+(count++)+")"+"."+destFileExt;
            }            
            String fName = f.getName();
            String fPath = f.getParent();
            // Now we need to check if given file name is valid for file system, and if it isn't we need to convert it to valid form
            if (!(testIfFileNameIsValid(destFileURI))) {
                List<String> forbidenCharsPatters = new ArrayList<String>();
                forbidenCharsPatters.add("[:]+"); // Mac OS, but it looks that also Windows XP
                forbidenCharsPatters.add("[\\*\"/\\\\\\[\\]\\:\\;\\|\\=\\,]+");  // Windows
                forbidenCharsPatters.add("[^\\w\\d\\.]+");  // last chance... only latin letters and digits
                for (String pattern:forbidenCharsPatters) {
                    String nameToTest = fName;
                    nameToTest = nameToTest.replaceAll(pattern, "_");
                    destFileURI=fPath+File.separator+nameToTest;
                    count=1;
                    destFileName = destFileURI.substring(0,destFileURI.lastIndexOf("."));
                    destFileExt = destFileURI.substring(destFileURI.lastIndexOf(".")+1);
                    while ((f=new File(destFileURI)).exists()) {
                        destFileURI=destFileName+"("+(count++)+")"+"."+destFileExt;
                    }
                    if (testIfFileNameIsValid(destFileURI)) break;
                }
            }            
            return destFileURI;
       }
        
        private static boolean testIfFileNameIsValid(String destFileURI) {
            boolean valid = false;
            try {
                File candidate = new File(destFileURI);                
                String canonicalPath = candidate.getCanonicalPath();                
                boolean b = candidate.createNewFile();
                if (b) {
                    candidate.delete();
                }
                valid = true;
            } catch (IOException ioEx) { }
            return valid;
        }
                
        public static OOoFormats findFormatForFilterName(String filterName) {
            OOoFormats[] formats =  OOoFormats.values();
            for (OOoFormats format:formats) {
                if (format.getFilterName().equals(filterName)) {
                    return format;
                }
            }
            return null;
        }
        
        public static boolean isMac() {
            String lcOSName = System.getProperty("os.name").toLowerCase();
            boolean MAC_OS_X = lcOSName.startsWith("mac os x");
            return MAC_OS_X;
        }
        
        public static String getJavaVersion() {
            return System.getProperty("java.version");
        }
        
        public static void openBrowserForURL(Component parent, final String url) {
            String java6 = "1.6.0";
            if (getJavaVersion().compareTo(java6)>=0) {
                // OK, we may use cool Desktop.getDesktop :-)
                try {                
                    Class desktopClass = Class.forName("java.awt.Desktop");
                    Method getDesktop = desktopClass.getMethod("getDesktop",(Class[])null);
                    Object desktop = getDesktop.invoke(null, (Object[])null);
                    Class[] paramsList = new Class[1];
                    paramsList[0] = URI.class;
                    Method browseMethod = desktop.getClass().getMethod("browse",paramsList);
                    Object[] parameters = new Object[1];
                    parameters[0]=new URI(url);
                    browseMethod.invoke(desktop, parameters);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    // ingore it, we will handle it in other way
                }
            }
            String browserExec = Configuration.getPathForBrowserExec(parent);
            
            String[] cmds = {"open", browserExec, url};                    
            try {
                Runtime.getRuntime().exec(cmds);
                Configuration.store();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(parent, "Problem: "+ioe.getMessage(),"Problem",JOptionPane.ERROR_MESSAGE);
            }
            
        }
}

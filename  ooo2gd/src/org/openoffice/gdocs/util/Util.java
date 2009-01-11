package org.openoffice.gdocs.util;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import java.io.IOException;

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
            sLoadUrl.append(fName.replace('\\', '/'));                              
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
        
        
        public static void openInOpenOffice(final String sLoadUrl, XFrame xFrame) throws com.sun.star.lang.IllegalArgumentException, com.sun.star.io.IOException {
            XComponentLoader loader = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class,xFrame);
            XComponent xComp = loader.loadComponentFromURL(sLoadUrl, "_blank", 0, new PropertyValue[0]);
            XTextDocument aTextDocument = (XTextDocument)UnoRuntime.queryInterface(com.sun.star.text.XTextDocument.class, xComp);
        }
        
            
        public static void startNewThread(ClassLoader classLoader, Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setContextClassLoader(classLoader);
            thread.start();        
        }
}

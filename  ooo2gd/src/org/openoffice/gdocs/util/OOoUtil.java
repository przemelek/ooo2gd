package org.openoffice.gdocs.util;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.adapter.XOutputStreamToByteArrayAdapter;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class OOoUtil {
    
        public static String fileNameToOOoURL(final String fName) {
            StringBuilder sLoadUrl = new StringBuilder("file:///");
            sLoadUrl.append(fName.replace('\\', '/'));                              
            return sLoadUrl.toString();
        }
    
        public static String convertDocumentToFormat(String pathName, String filterName, String newExt,XFrame xFrame ) throws com.sun.star.io.IOException, com.sun.star.lang.IllegalArgumentException,IOException {
            String fName = OOoUtil.fileNameToOOoURL(pathName);
            String newPathName = pathName.substring(0,pathName.lastIndexOf("."))+"."+newExt;
            String newFName = OOoUtil.fileNameToOOoURL(newPathName);

            PropertyValue[] properties = new PropertyValue[3];
            PropertyValue prop = new PropertyValue();
            prop.Name="Hidden";
            prop.Value=true;
            properties[0]=prop;
            properties[1]=new PropertyValue();
            properties[1].Name="ReadOnly";
            properties[1].Value=new Boolean(true);
            properties[2]=new PropertyValue();
            properties[2].Value="AsTemplate";
            properties[2].Value=new Boolean(true);
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
            propertyValue[0].Value = new Boolean(true);
            propertyValue[1] = new com.sun.star.beans.PropertyValue();
            propertyValue[1].Name = "FilterName";
            propertyValue[1].Value = filterName;
            propertyValue[2]=new PropertyValue();
            propertyValue[2].Value="AsTemplate";
            propertyValue[2].Value=new Boolean(true);            
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
}

package org.openoffice.gdocs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Inspired by http://bordet.blogspot.com/2007/01/utf-8-handling-for-resourcebundle-and.html
 */
public class EncodingSensitiveControl extends ResourceBundle.Control {
  private static final String FORMAT_SUFFIX = "properties.";
  private final List<String> FORMATS;
  
  public static final String[] STANDARD_ENCODINGS = {"","US-ASCII", "ISO-8859-1", "ISO-8859-2", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"};
  public static final String[] ALL_ENCODINGS = Charset.availableCharsets().keySet().toArray(new String[0]);
  public EncodingSensitiveControl() {
    this(STANDARD_ENCODINGS);
  }
  
  public EncodingSensitiveControl(String[] encodingNames) {
    List<String> formats = new ArrayList<String>(ResourceBundle.Control.FORMAT_DEFAULT);
    for(String encodingName : encodingNames){
      formats.add("java."+FORMAT_SUFFIX+encodingName.toLowerCase());
    }
    FORMATS = Collections.unmodifiableList(formats);
  }

  @Override
  public List<String> getFormats(String baseName) {
    return FORMATS;
  }
  
//  @Override
//  public long getTimeToLive(String name, Locale locale){
//    return -1;
//  }
  
  @Override
  public ResourceBundle newBundle(String baseName, Locale locale,
      String format, ClassLoader loader, boolean reload)
      throws IllegalAccessException, InstantiationException, IOException {
    if (!FORMATS.contains(format))
      return super.newBundle(baseName, locale, format, loader, reload);

    String bundleName = toBundleName(baseName, locale);
    String resourceName = toResourceName(bundleName, format.substring(5));
    final URL resourceURL = loader.getResource(resourceName);
    if (resourceURL == null)
      return null;

    if(FORMAT_PROPERTIES.equals(format)){
      return super.newBundle(baseName, locale, format, loader, reload);
    }
    InputStream stream = getResourceInputStream(resourceURL, reload);

    try {
      EncodedPropertyResourceBundle result = 
        new EncodedPropertyResourceBundle(format.substring(format.lastIndexOf('.')+1));
      result.load(stream);
      return result;
    } finally {
      stream.close();
    }
  }
  
  private InputStream getResourceInputStream(final java.net.URL resourceURL,
      boolean reload) throws IOException {
    if (!reload)
      return resourceURL.openStream();

    try {
      // This permission has already been checked by
      // ClassLoader.getResource(String), which will return null
      // in case the code has not enough privileges.
      return AccessController
          .doPrivileged(new PrivilegedExceptionAction<InputStream>() {
            public InputStream run() throws IOException {
              URLConnection connection = resourceURL.openConnection();
              connection.setUseCaches(false);
              return connection.getInputStream();
            }
          });
    } catch (PrivilegedActionException x) {
      throw (IOException) x.getCause();
    }
  }
  
  public static class EncodedPropertyResourceBundle extends ResourceBundle {
    private final Properties properties = new Properties();
    
    private final String encodingName;
    public EncodedPropertyResourceBundle(String encodingName) {
      this.encodingName = encodingName;
    }

    public void load(InputStream stream) throws IOException {
      properties.load(new InputStreamReader(stream, encodingName));
    }

    protected Object handleGetObject(String key) {
      return properties.getProperty(key);
    }

    public Enumeration<String> getKeys() {
      final Enumeration<Object> keys = properties.keys();
      return new Enumeration<String>() {
        public boolean hasMoreElements() {
          return keys.hasMoreElements();
        }

        public String nextElement() {
          return (String) keys.nextElement();
        }
      };
    }
  }
}
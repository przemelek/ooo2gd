package org.openoffice.gdocs.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openoffice.gdocs.configuration.Configuration;

//import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.HttpStatus;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;

import com.google.gdata.client.http.HttpAuthToken;

public class Downloader implements Runnable {

    protected int getContentLength(HttpURLConnection conn) {
        String contentLengthStr = conn.getHeaderField("Content-Length");
        int contentLength = IOEvent.getUNKNOWN_SIZE();
        if (contentLengthStr != null) {
            try {
                contentLength = Integer.parseInt(contentLengthStr);
            } catch (NumberFormatException nfex) {
                System.err.println("Warning: can not parse the content lenght; continuing with download.");
                nfex.printStackTrace();
            }
        }
        return contentLength;
    }
  private interface DownloaderInterface {
      public void fetchImpl(InputStream is, int contentLength) throws IOException;
  }
  private static final int DEFAULT_RETRY_COUNT = 5;
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
  private static final String DEFAULT_USER_AGENT = "ooo2gd";
  
  private static final IOListener[] DUMMY_IOLISTENER_ARRAY = {};
    
  private final Wrapper wrapper;
  private HttpAuthToken authToken;
  private URL source;
  private URI destFileURI;
  
  private String userAgent = DEFAULT_USER_AGENT;
  
  private List<IOListener> ioListeners = Collections.synchronizedList(new ArrayList<IOListener>());
  
  private DownloaderInterface currentDownloader;

  
  public Downloader(URI source, String destFileURI, Wrapper wrapper) throws URISyntaxException, MalformedURLException {
      this.source = source.toURL();
      this.destFileURI = new File(destFileURI).toURI();
//    if(!(docsService.getAuthTokenFactory().getAuthToken() instanceof HttpAuthToken)){
//       throw new IllegalArgumentException("The downloader class works only with HttpAuthTokens.");
//    }
      this.wrapper = wrapper;
      this.currentDownloader = new DownloaderInterface() {
         public void fetchImpl(InputStream is, int contentLength) throws IOException {
             fetchImplForFile(is,contentLength);
         }
      };    
  }
  
  public Downloader(URI source, final OutputStream out, Wrapper wrapper) throws MalformedURLException{
      this.source = source.toURL();      
      this.wrapper = wrapper; 
      this.currentDownloader = new DownloaderInterface() {
         public void fetchImpl(InputStream is, int contentLength) throws IOException {
             fetchImplForMemory(is,out,contentLength);
         }      
    };
  }
  
  public void download() throws IOException {          
      doFetchHttpURLConnection();      
  }
  
  //public Downloader()
  
  //public Downloader(String source, String destFileURI, DocsService docsService) throws MalformedURLException, URISyntaxException {
 //   this(new URI(source), destFileURI, docsService);
  //}
  
  public void addIOListener(IOListener l){
    ioListeners.add(l);
  }
  
  protected void fireIOEvent(IOEvent ioEvent){
    IOListener[] listeners = ioListeners.toArray(DUMMY_IOLISTENER_ARRAY);
    for(IOListener listener : listeners){
      listener.ioProgress(ioEvent);
    }
  }
  
  public void start() {
    Thread t = new Thread(this, "Document fetcher thread");
    t.setContextClassLoader(Configuration.getClassLoader());
    t.setDaemon(true);
    t.start();
  }

  public void run() {
    try {
      doFetchHttpURLConnection();      
    } catch (IOException ioex) {
      ioex.printStackTrace();
      fireIOEvent(new IOEvent(this, 
                              IOEvent.getUNKNOWN_SIZE(), 
                              IOEvent.getUNKNOWN_SIZE(),
                              ioex, 
                              Configuration.getResources().getString("ERROR_IMPORTING_FILE")
                              )
                             );
    }
  }

  private void doFetchHttpURLConnection() throws IOException {
    HttpURLConnection.setDefaultAllowUserInteraction(true);
    
    HttpURLConnection conn = (HttpURLConnection)source.openConnection();
    setAthenticationHeader(conn);
    conn.setRequestProperty("User-Agent", userAgent);
    conn.setRequestMethod("GET");
    conn.connect();
    int contentLength = getContentLength(conn);
    InputStream is = conn.getInputStream();
    //fetchImpl(is, contentLength);
    currentDownloader.fetchImpl(is, contentLength);
  }
  
  protected void setAthenticationHeader(HttpURLConnection conn) {
        HttpAuthToken authToken = getAuthToken();
        String header = authToken.getAuthorizationHeader(source, "GET");
        conn.setRequestProperty("Authorization", header);    
}
 
  
  /**
   * Works only for file based targets. We assume user have write permission for the directory containing
   * the destination file.
   * @param contentLength 
   * @throws IOException 
   */
  private void fetchImplForFile(InputStream is, int contentLength) throws IOException {
    File destFile = new File(destFileURI);
    File tempFile = createTempFile(destFile);
    int progress = 0;
    OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
    try {      
       progress = getStream(is, contentLength, out);
    } finally {
      if(out!=null){
        out.close();
      }
    }       
      if(destFile.exists()&&!destFile.canWrite()){
        throw new IOException("Can not write to the destination file.");
      }
      if(!tempFile.renameTo(destFile)){
        forcedCopy(tempFile, destFile);
      }
      tempFile.delete(); // we need not to wait for exit
      fireIOEvent(new IOEvent(this,contentLength,progress,null,null,true));
  }
  
  private void fetchImplForMemory(InputStream is, OutputStream out, int contentLength) throws IOException {
    int progress = 0;    
    try {      
       progress = getStream(is, contentLength, out);
    } finally {
      if(out!=null){
        out.close();
      }
    }       
   fireIOEvent(new IOEvent(this,contentLength,progress,null,null,true));      
  }

    private int getStream(final InputStream is, final int contentLength, OutputStream out) throws IOException {
        int progress = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];            
        int readCount;        
        while((readCount=is.read(buffer))>0) {
            String bufStr = new String(buffer);
          out.write(buffer, 0, readCount);
          progress += readCount;
          fireIOEvent(new IOEvent(this, contentLength, progress, null, null));
        }
        out.flush();
        out.close();
        out = null;
        return progress;
    }
  
  /**
   * Copied from http://www.javalobby.org/java/forums/t17036.html
   * @param sourceFile
   * @param destFile
   * @throws IOException
   */
  private void forcedCopy(File sourceFile, File destFile) throws IOException {
    if(!destFile.exists()) {
      destFile.createNewFile();
    }
    
    FileChannel source = null;
    FileChannel destination = null;
    try {
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
    } finally {
      if(source != null) {
        source.close();
      }
      if(destination != null) {
        destination.close();
      }
    }
  }
  
  private static final String TEMP_FILE_EXTENSION = "part";
  private static final int MAXIMUM_TEMP_FILE_ATTEMPTS = 0x10000; // 0x0000 - 0xFFFF
  private File createTempFile(File destFile){
    File tempFile = null;
    for(int i=0; i<MAXIMUM_TEMP_FILE_ATTEMPTS; i++){
      File f = new File(destFile.getParent(), destFile.getName()+"."+Integer.toHexString(i)+"."+TEMP_FILE_EXTENSION);
      if(!f.exists()){
        tempFile = f;
        break;
      }
    }
    if(tempFile==null){
      // Really unlikely situation. 
      // Just to make sure we are not going into infinite loop.
      throw new IllegalStateException("Can not create temporary file."); 
    }
    tempFile.deleteOnExit();
    return tempFile;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getUserAgent() {
    return userAgent;
  }
  
  public void setAuthToken(HttpAuthToken token) {
    this.authToken = token;
  }
  
  public HttpAuthToken getAuthToken() {
      return this.authToken;
  }
  
}

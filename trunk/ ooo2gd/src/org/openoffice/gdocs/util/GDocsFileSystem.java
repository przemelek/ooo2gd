
package org.openoffice.gdocs.util;

import com.google.gdata.data.docs.DocumentListEntry;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class GDocsFileSystem extends FileSystemView{
    
    private GoogleDocsWrapper wrapper;
    
    public class GDocsFile extends File {
            public GDocsFile(String fName) {
                super(fName);
            }
            private DocumentListEntry entry;
            public void setEntry(DocumentListEntry entry) {
                this.entry = entry;
            }
            public long lastModified() {
                return entry.getUpdated().getValue();
            }
            public DocumentListEntry getEntry() {
                return this.entry;
            }

        public boolean isFile() {
            return true;
        }

        public boolean isDirectory() {
            return false;
        }        
    }

    public static FileSystemView getFileSystemView() {
        System.out.println("getFileSystemView()");
        return new GDocsFileSystem();
    }    
    
    
    public GDocsFileSystem() {        
    }
    
    /** Creates a new instance of GDocsFileSystem */
    public GDocsFileSystem(GoogleDocsWrapper wrapper) {
        this.wrapper = wrapper;        
    }

    public File createNewFolder(File containingDir) throws IOException {
        return null;
    }
    
    
    
    public File[] getFiles(File dir, boolean useFileHiding) {
            System.out.println(dir.getName());
            File[] files = null;
            try {
                List<DocumentListEntry> list =  wrapper.getListOfDocs();
                List<File> filesList = new ArrayList<File>();
                for (DocumentListEntry entry:list) {
                    GDocsFile file = new GDocsFile(entry.getTitle().getPlainText());
                    
                    file.setLastModified(entry.getUpdated().getValue());
                    file.setEntry(entry);
                    //file.setLastModified()
                    filesList.add(file);
                }
                files = new File[filesList.size()];
                files = filesList.toArray(files);
            } catch (Exception e) { 
                e.printStackTrace();
            }
            return files;
    }    

    protected File createFileSystemRoot(File f) {
        System.out.println("createFileSystemRoot");
        return null;
    }

    public File createFileObject(File dir, String filename) {
        System.out.println("createFileObject");
        return null;
    }

    public File createFileObject(String path) {
        System.out.println(path); 
        return new File(path);
    }

    public File getChild(File parent, String fileName) {
        System.out.println(parent.getPath()+" "+fileName);
        return null;
    }

    public File[] getRoots() {
        File[] files = new File[1];
        files[0] = new File("Google Docs");
        return files;
    }

    public File getHomeDirectory() {
        return new File("Google Docs");
    }

    
    public File getParentDirectory(File dir) {
        System.out.println("getParentDirectory");
        return null;        
    }

    protected File createFileSystemRoot() {
        System.out.println("createFileSystemRoot()");
        return null;
    }


    
//    public File getDefaultDirectory() {
//        System.out.println("getDefaultDirectory");
//        File file = new File("Google Docs");
//        return file;
//    }
    


    
    public static void main(String[] args) throws Exception {
        GoogleDocsWrapper wrapper = new GoogleDocsWrapper();
        Creditionals credits = new Creditionals("user","password");
        wrapper.login(credits);
        GDocsFileSystem fileSystem = new GDocsFileSystem(wrapper);
        JFileChooser fileChooser = new JFileChooser(new File("Google Docs"),fileSystem);        
        fileChooser.showOpenDialog(null);
        URI uri = wrapper.getUriForEntry(((GDocsFile)fileChooser.getSelectedFile()).getEntry(),credits );
        System.out.println(uri);
        
    }
}

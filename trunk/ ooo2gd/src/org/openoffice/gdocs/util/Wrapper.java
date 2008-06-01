package org.openoffice.gdocs.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public interface Wrapper {

    public Downloader getDownloader(URI uri, String documentUrl) throws URISyntaxException, MalformedURLException;
    public URI getUriForEntry(final Document entry) throws URISyntaxException;
    public URI getUriForEntryInBrowser(final Document entry) throws URISyntaxException;
    public void login(Creditionals creditionals) throws Exception;
    public boolean upload(String path,String documentTitle) throws Exception;
    public boolean neeedConversion(String path);
    public String closestSupportedFormat(String path);
    public String getSystem();
    public List<Document> getListOfDocs() throws Exception;
}

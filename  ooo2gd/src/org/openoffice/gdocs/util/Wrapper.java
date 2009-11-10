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
    public boolean checkIfAuthorizationNeeded(String path,String documentTitle) throws Exception;
    public boolean upload(String path,String documentTitle,String mimeType) throws Exception;
    public boolean neededConversion(String path);
    public boolean neededConversion(OOoFormats format);
    public OOoFormats convertTo(OOoFormats format);
    public String closestSupportedFormat(String path);
    public String getSystem();
    public List<Document> getListOfDocs(boolean useCachedListIfPossible) throws Exception;
    public void storeCredentials(Creditionals credentials);
    public void setServerPath(String serverPath);
    public boolean isServerSelectionNeeded();
    public List<String> getListOfServersForSelection();
    public Creditionals getCreditionalsForServer(String serverPath);
    public boolean updateSupported();
    public boolean update(String path,String docId,String mimeType) throws Exception;
    public boolean downloadInGivenFormatSupported();
    public List<OOoFormats> getListOfSupportedForDownloadFormatsForEntry(Document entry);
    public URI getUriForEntry(final Document entry, final OOoFormats format) throws URISyntaxException;
    public boolean hasList();
}

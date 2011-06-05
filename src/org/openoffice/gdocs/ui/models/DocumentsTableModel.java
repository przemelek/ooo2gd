// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.ui.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.util.Document;
import org.openoffice.gdocs.util.Wrapper;

public class DocumentsTableModel extends DefaultTableModel {

    private List<Document> list = new ArrayList<Document>();
    private Wrapper wrapper;
    private int numberOfColumns;
    private String filter;
    private String folderFilter;
    private DateFormat df;
    private DateFormat parseDf;
    private Map<String,String> folder2color = new HashMap<String, String>();
    private static String[] colors = {"yellow|black","green|white","orange|black","cyan|black","purple|white","red|white"};
    private int lastColor = 0;


    public DocumentsTableModel() {
        this(null);
    }
    
    public DocumentsTableModel(Wrapper wrapper) {
        this.wrapper=wrapper;
        numberOfColumns=2;
//        if (wrapper!=null && "Google Docs".equals(wrapper.getSystem())) {
//            numberOfColumns = 3;
//        }
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class<?> type = String.class;
        return type;
    }
    
    public void setFilter(String str) {
        this.filter = str;
    }

    public void setFolderFilter(String folderFilter) {
        this.folderFilter = folderFilter;
    }

    private boolean isOneOf(String str,String... strings) {
        return java.util.Arrays.asList(strings).contains(str);
//        for (String s:strings) {
//            if (s.equals(str)) return true;
//        }
//        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Document entry = getList().get(rowIndex);
        Object obj = null;
        String date = entry.getUpdated();
        try {
//            date = df.format(wrapper.parseDate(entry.getUpdated().replace("Z", "-00:00Z")));
            date = df.format(wrapper.parseDate(entry.getUpdated()));
        } catch (ParseException pe) {
            Configuration.log("Problem with parsing date "+entry.getUpdated());
        }
        String folders = "";
        if (wrapper!=null && "Google Docs".equals(wrapper.getSystem())) {            
            for (String folder:entry.getFolders()) {
                if (folder==null) continue;
                if (isOneOf(folder,"document","presentation","spreadsheet","viewed")) continue;
                String color = folder2color.get(folder);
                if (color==null) {
                    if (lastColor<colors.length) {
                        color = colors[lastColor++];                        
                    } else {
                        color="gray|white";
                    }
                    folder2color.put(folder, color);
                }
                String[] fontColors = color.split("\\|");
                folders+="<i><font bgcolor=\""+fontColors[0]+"\" color=\""+fontColors[1]+"\">&nbsp;"+folder+"&nbsp;</font></i>";
            }
        }
        String name = entry.getTitle();
        if (!"".equals(folders)) {
            name="<html><body>"+name+" "+folders;
        }
        switch (columnIndex) {
            case 0: obj = name; break;
            case 1: obj = date;
        }
        if (obj==null) obj="";
        return obj;
    }

    public int getColumnCount() {
        return numberOfColumns;
    }

    public int getRowCount() {
        int result = 0;
        if (getList()!=null) {
            result = getList().size();
        }
        return result;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0: return Configuration.getResources().getString("Document_Title");
            case 1: return Configuration.getResources().getString("Published");
        }
        return "";
    }
       
    public void add(Document entry) {
        String folders = "";
        if (wrapper!=null && "Google Docs".equals(wrapper.getSystem())) {
            for (String folder:entry.getFolders()) {
                if (folder==null) continue;
                if (isOneOf(folder,"document","presentation","spreadsheet","viewed")) continue;
                String color = folder2color.get(folder);
                if (color==null) {
                    if (lastColor<colors.length) {
                        color = colors[lastColor++];
                    } else {
                        color="gray|white";
                    }
                    folder2color.put(folder, color);
                }
            }
        }
        getList().add(entry);
    }

    public Document getEntry(int idx) {
        Document document = null;
        if (idx>=0 && idx<getList().size()) {
            document = getList().get(idx);
        }
        return document;
    }

    /**
     * @return the list
     */
    public List<Document> getList() {
        List<Document> filteredList = list;
        if (filter!=null && filter.length()!=0) {
                 filteredList = new ArrayList<Document>();
                 for (Document doc:list) {
                     if (doc.getTitle().toUpperCase().contains(filter)) {
                         filteredList.add(doc);
                     }
                 }
        }
        if (folderFilter!=null && folderFilter.length()!=0) {
            List<Document> newFilteredList = new ArrayList<Document>();
            for (Document doc:filteredList) {
                if (doc.getFolders().contains(folderFilter)) {
                    newFilteredList.add(doc);
                }
            }
            filteredList = newFilteredList;
        }
        return filteredList;
    }

    /**
     * @param list the list to set
     */
    public void setList(List<Document> list) {
        this.list = list;
    }

    public Map<String,String> getFolders2ColorsMap() {
        return folder2color;
    }
}
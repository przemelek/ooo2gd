// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.ui.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.util.Document;
import org.openoffice.gdocs.util.Wrapper;

public class DocumentsTableModel extends DefaultTableModel {

    private List<Document> list = new ArrayList<Document>();
    private Wrapper wrapper;
    private int numberOfColumns;
    private String filter;
    private DateFormat df;
    private DateFormat parseDf;
    
    public DocumentsTableModel() {
        this(null);
    }
    
    public DocumentsTableModel(Wrapper wrapper) {
        this.wrapper=wrapper;
        numberOfColumns=2;
        df = DateFormat.getDateTimeInstance();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class<?> type = String.class;
        return type;
    }
    
    public void setFilter(String str) {
        this.filter = str;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Document entry = getList().get(rowIndex);
        Object obj = null;
        String date = entry.getUpdated();
        try {
            date = df.format(wrapper.parseDate(entry.getUpdated()));
        } catch (ParseException pe) {
            Configuration.log("Problem with parsing date "+entry.getUpdated());
        }

        switch (columnIndex) {
            case 0: obj = entry.getTitle(); break;
            case 1: obj = date; break;
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
        return filteredList;
    }

    /**
     * @param list the list to set
     */
    public void setList(List<Document> list) {
        this.list = list;
    }
    
}
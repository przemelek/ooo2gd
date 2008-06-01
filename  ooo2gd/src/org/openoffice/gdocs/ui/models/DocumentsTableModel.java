// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.ui.models;

import com.google.gdata.data.docs.DocumentListEntry;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openoffice.gdocs.configuration.Configuration;
import org.openoffice.gdocs.util.Document;

public class DocumentsTableModel extends AbstractTableModel {

    private List<Document> list = new ArrayList<Document>();
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Document entry = list.get(rowIndex);
        Object obj = null;            
        switch (columnIndex) {
            case 0: obj = entry.getTitle(); break;
            case 1: obj = entry.getUpdated(); break;
        }
        if (obj==null) obj="";
        return obj;
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        int result = 0;
        if (list!=null) {
            result = list.size();
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
        list.add(entry);
    }

    public Document getEntry(int idx) {            
        return list.get(idx);
    }
    
}
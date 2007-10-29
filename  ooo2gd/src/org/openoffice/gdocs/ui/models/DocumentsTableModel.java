package org.openoffice.gdocs.ui.models;

import com.google.gdata.data.docs.DocumentListEntry;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;


public class DocumentsTableModel extends AbstractTableModel {

    private List<DocumentListEntry> list = new ArrayList<DocumentListEntry>();
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        DocumentListEntry entry = list.get(rowIndex);
        Object obj = null;            
        switch (columnIndex) {
            case 0: obj = entry.getTitle().getPlainText(); break;
            case 1: obj = entry.getUpdated().toStringRfc822(); break;
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
            case 0: return "Document Title";
            case 1: return "Published";
        }
        return "";
    }
       
    public void add(DocumentListEntry entry) {
        list.add(entry);
    }

    public DocumentListEntry getEntry(int idx) {            
        return list.get(idx);
    }
    
}
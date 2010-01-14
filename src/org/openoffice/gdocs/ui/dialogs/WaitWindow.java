package org.openoffice.gdocs.ui.dialogs;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class WaitWindow extends JDialog {
    
    public WaitWindow() {
        setUndecorated(true);
        JLabel label = new JLabel("Please wait...");
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(label);
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();  
            Dimension screenSize = toolkit.getScreenSize();
            this.setLocation((int)screenSize.getWidth()/2-this.getWidth()/2,(int)screenSize.getHeight()/2-this.getHeight()/2);
        } catch (Exception e) {
            // we will ignore this
        }        
        pack();
    }        
}

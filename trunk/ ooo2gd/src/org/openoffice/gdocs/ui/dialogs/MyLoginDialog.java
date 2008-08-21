package org.openoffice.gdocs.ui.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

class MyLoginDialog extends JDialog {
        private int returnCode;
        private String returnValue;
        public MyLoginDialog(JFrame owner) {
            super(owner);
        }

        public void handleOK() {
            this.returnCode = JOptionPane.OK_OPTION;
            dispose();
        }
        public void handleCancel() {
            this.returnCode = JOptionPane.OK_CANCEL_OPTION;
            dispose();                                
        }
        
       public int getReturnCode() {
           return this.returnCode;
       }
       public void setReturnValue(String str) {
           this.returnValue = str;
       }
       public String getReturnValue() {
           return this.returnValue;
       }
    }

package org.openoffice.gdocs.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CaptchaDialog extends JDialog {
    private String captchaImageURL;
    private JTextField captchaTextField = new JTextField();
    private int returnCode;
    private String returnValue;
    public CaptchaDialog(String url) {
        this.captchaImageURL=url;
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel(" Write letters from picture. "),BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("<html><img src=\""+captchaImageURL+"\"/></body>"));
        panel.add(captchaTextField,BorderLayout.SOUTH);
        getContentPane().add(panel);
        JPanel buttonsPanel = new JPanel();
        final CaptchaDialog dialog = this;
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.handleOK();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.handleCancel();
            }
        });
        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(okButton,BorderLayout.WEST);
        buttonsPanel.add(cancelButton,BorderLayout.EAST);
        getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
        pack();
    }
    
        public void handleOK() {
            this.returnValue = captchaTextField.getText();
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
        
        public String getReturnValue() {
            return this.returnValue;
        }        
}

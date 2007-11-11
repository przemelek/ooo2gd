// (c) 2007 by Przemyslaw Rumik
// myBlog: http://przemelek.blogspot.com
// project page: http://ooo2gd.googlecode.com
// contact with me: http://przemelek.googlepages.com/kontakt
package org.openoffice.gdocs.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Creditionals {
    
    private static final String CREDITIONALS_FILE = "gdocs.dat";
    private static final String SECRET_PHRASE = "$ogorek#";
    private boolean wasCreditionalsReadedFromDisk = false;
    private String userName;
    private String password;

    public Creditionals() {
        super();
        setWasCreditionalReadFromDisk(readCreditionals());
    }    
    
    public Creditionals(String userName, String password) {
        super();
        this.userName = userName;
        this.password = password;
    }

        private String xorString(String input,String key) {
        char[] keyChars = key.toCharArray();
        char[] inputChars = input.toCharArray();
        for (int i=0; i<inputChars.length; i++) {
            inputChars[i]^=keyChars[i%keyChars.length];
        }
        return new String(inputChars);
    }
    
    private boolean readCreditionals() {
        boolean result = false;
        try {            
            File file = new File(CREDITIONALS_FILE);            
            if (file.exists()) {
                FileReader fr = new FileReader(CREDITIONALS_FILE);                
                BufferedReader br = new BufferedReader(fr);
                char[] buf=new char[1024];
                int length = br.read(buf);
                buf = Arrays.copyOf(buf,length);
                br.close();
                String decoded = xorString(new String(buf),SECRET_PHRASE);
                String[] lines = decoded.split("\n");
                setUserName(lines[0]);
                setPassword(lines[1]);
                result=true;
            }
        } catch (Exception e) {};
        return result;
    }
    
    private void storeCreditionals() {
        try {
            FileWriter fw = new FileWriter(CREDITIONALS_FILE);
            BufferedWriter bw = new BufferedWriter(fw);
            String password = getPassword();
            String userName = getUserName();
            String str = userName+"\n"+password;
            bw.write(xorString(str,SECRET_PHRASE));
            bw.close();
        } catch (IOException e) {
            // Intentionaly left empty
            // If IOException will be thrown we ignore this
        }
    }    

    public void store() {
        storeCreditionals();
    }
    
    public String getUserName() {
        return userName;
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }
    
    private void setWasCreditionalReadFromDisk(boolean flag) {
        wasCreditionalsReadedFromDisk = flag;
    }
    
    public boolean getWsCreditionalsReadedFromDisk() {
        return wasCreditionalsReadedFromDisk;
    }
}

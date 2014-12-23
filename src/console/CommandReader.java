/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tristan
 * @date Mar 16, 2012
 */
public class CommandReader implements Runnable {
    private InputStream stream;
    private BufferedReader reader;
    private String currentInput;
    private String[] prevInputs;
    private String cursor = "> ";
    private boolean running = false;
    
    public CommandReader(InputStream stream){
        reader = new BufferedReader(new InputStreamReader(stream));
        this.stream = stream;
        prevInputs = new String[0];
        currentInput = "";
    }
    
    public InputStream getStream(){
        return stream;
    }
    
    @Override
    public void run(){
        enable();
        while(running){
            try {
                int charCode = reader.read();
                if(charCode != -1){
                    char key = (char) charCode;
                    if(key == (char)13 && currentInput != null && !currentInput.equals("")) {
                        String[] tempInputs = prevInputs;
                        prevInputs = new String[tempInputs.length];
                        prevInputs[tempInputs.length] = currentInput;
                    } else if(key == (char)8) {
                        currentInput = currentInput.substring(0, currentInput.length() - 2);
                    } else {
                        currentInput += key;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(CommandReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void enable(){
        System.out.print(cursor);
        prevInputs = new String[0];
        currentInput = "";
        running = true;
    }
    
    public void disable(){
        int toBackSpace = cursor.length() + currentInput.length();
        for(int r = 0; r < toBackSpace; r++){
            System.out.print("\r");
        }
        running = false;
    }
    
    public void setCursor(String c){
        cursor = c;
    }
    
    public String getCursor(){
        return cursor;
    }
    
    public String getCurrentInput(){
        return currentInput;
    }
    
    public String getLine(){
        String ret = null;
        if(prevInputs.length > 0){
            ret = prevInputs[0] + "\n";
            String[] tempInputs = prevInputs;
            prevInputs = new String[tempInputs.length - 2];
            for(int i = 1; i < tempInputs.length; i++){
                prevInputs[i - 1] = tempInputs[i];
            }
        }
        return ret;
    }
}

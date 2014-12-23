/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package console;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author Tristan
 * @date Mar 16, 2012
 */
public class OutputHandler extends PrintStream {

    private CommandReader input;

    public OutputHandler(OutputStream out, CommandReader in) {
        super(out);
        input = in;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        int toBackSpace = input.getCursor().length() + input.getCurrentInput().length();
        for (int r = 0; r < toBackSpace; r++) {
            super.write(new byte[]{'\r'});
        }
        super.write(bytes);
        super.write(input.getCursor().getBytes());
        super.write(input.getCurrentInput().getBytes());
    }
}

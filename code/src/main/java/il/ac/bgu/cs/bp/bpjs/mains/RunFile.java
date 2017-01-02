/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.mains;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Scriptable;

/**
 * Run a single program file. Outputs event log to (@code stdout}.
 * 
 * @author michael
 */
public class RunFile {
    
    public static void main(String[] args) {
        if ( args.length == 0 ) {
            printUsageAndExit();
        }
        
        // Get the program as stream
        final String sourceName;
        final InputStream in;
        boolean closeIn = false;
        
        if ( args[0].equals("-") ) {
            sourceName = "stdin";
            in = System.in;
        } else {
            Path inFile = Paths.get(args[0]);
            sourceName = inFile.toAbsolutePath().toString();
            try {
                in = new FileInputStream(inFile.toFile());
            } catch (FileNotFoundException ex) {
                System.out.println("File " + sourceName + " not found.");
                System.exit(-2);
                return; // needed for static analysis.
            }
            closeIn = true;
        }
        System.out.println("Reading from " + sourceName);
        
        try {
            BProgram bpp = new BProgram(sourceName){
                @Override
                protected void setupProgramScope(Scriptable scope) {
                    evaluateInGlobalScope(in, sourceName);
                }
            };
            
           bpp.addListener( new StreamLoggerListener() );
           bpp.setName(sourceName);
           bpp.start();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(RunFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if ( closeIn ) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(RunFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }

    private static void printUsageAndExit() {
        System.out.println("Runs a BPjs program. First argument has to be a path"
                + " to the executable file. use `-` to read from standard in.");
        System.exit(-1);
    }
    
}

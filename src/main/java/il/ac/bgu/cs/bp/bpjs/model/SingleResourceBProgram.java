/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import java.net.URL;
import org.mozilla.javascript.Scriptable;

/**
 * Convenience class for BPrograms that consist of a single Javascript file.
 * @author michael
 */
public class SingleResourceBProgram extends BProgram {
    
    private final String resourceName;

    public SingleResourceBProgram(String aResourceName) {
        super( aResourceName );
        resourceName = aResourceName;
        URL resUrl = Thread.currentThread().getContextClassLoader().getResource(aResourceName);
            
        if ( resUrl == null ) {
            throw new IllegalArgumentException( "Cannot find resource '" + aResourceName + "'");    
        }
    }
    
    public SingleResourceBProgram(String aResourceName, String aName) {
        super(aName);
        resourceName = aResourceName;
    }
   
    public SingleResourceBProgram(String aResourceName, String aName, EventSelectionStrategy ess) {
        super(aName, ess);
        resourceName = aResourceName;
    }
    
    @Override
    protected void setupProgramScope(Scriptable scope) {
        evaluateResource( resourceName );
    }
    
}

/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import static java.util.stream.Collectors.joining;
import org.mozilla.javascript.Scriptable;

/**
 * Convenience class for BPrograms that consist of JavaScript file stored as
 * resources in the application.
 * 
 * @author michael
 */
public class ResourceBProgram extends BProgram {
    
    private Collection<String> resourceNames;

    public ResourceBProgram(String aResourceName, String aName, EventSelectionStrategy ess) {
        this( Collections.singletonList(aResourceName), aName, ess);
    }
    
    public ResourceBProgram(String aResourceName, EventSelectionStrategy ess) {
        this( Collections.singletonList(aResourceName), aResourceName, ess);
    }
    
    public ResourceBProgram( String... resourceNames ) {
        this( Arrays.asList(resourceNames), resourceNames[0]+"+", null );
    }
    
    public ResourceBProgram(Collection<String> someResourceNames) {
        this(someResourceNames, someResourceNames.stream().collect(joining("+")), null);
    }
    
    public ResourceBProgram(Collection<String> someResourceNames, String aName, EventSelectionStrategy ess) {
        super(aName, ess);        
        resourceNames = someResourceNames;
        resourceNames.forEach(this::verifyResourceExists);
    }
    
    @Override
    protected void setupProgramScope(Scriptable scope) {
        resourceNames.forEach(name->{
            try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
                if (resource == null) {
                    throw new RuntimeException("Resource '" + name + "' not found.");
                }
                evaluate(resource, name);
            } catch (IOException ex) {
                throw new RuntimeException("Error reading resource: '" + name + "': " + ex.getMessage(), ex);
            }
        });
        
        resourceNames = null; // free memory
    }
    
    private void verifyResourceExists( String resName ) {
        URL resUrl = Thread.currentThread().getContextClassLoader().getResource(resName);
            
        if ( resUrl == null ) {
            throw new IllegalArgumentException( "Cannot find resource '" + resName + "'");    
        }
    }
    
}

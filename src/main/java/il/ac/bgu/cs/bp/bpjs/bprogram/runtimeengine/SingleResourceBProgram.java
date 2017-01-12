/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionStrategy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.mozilla.javascript.Scriptable;

/**
 * Convenience class for BPrograms that consist of a single Javascript file.
 * @author michael
 */
public class SingleResourceBProgram extends BProgram {
    
    private final URI resourceUri;

    public SingleResourceBProgram(URI resourceUri) {
        super(resourceUri.getPath());
        this.resourceUri = resourceUri;
    }

    public SingleResourceBProgram(String resourceName) {
        super( resourceName );
        try {
            final URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourceName);
            if ( resourceUrl == null ) {
                throw new IllegalArgumentException("Specified resource '" + resourceName + "' not available in classpath.");
            }
            resourceUri = resourceUrl.toURI();
            
        } catch ( URISyntaxException use ) {
            throw new RuntimeException( use );
        }
    }
    
    public SingleResourceBProgram(URI resourceUri, String aName) {
        super(aName);
        this.resourceUri = resourceUri;
    }

    public SingleResourceBProgram(URI resourceUri, String aName, EventSelectionStrategy anEventSelectionStrategy) {
        super(aName, anEventSelectionStrategy);
        this.resourceUri = resourceUri;
    }

    @Override
    protected void setupProgramScope(Scriptable scope) {
        evaluateCodeAt( resourceUri );
    }
    
}

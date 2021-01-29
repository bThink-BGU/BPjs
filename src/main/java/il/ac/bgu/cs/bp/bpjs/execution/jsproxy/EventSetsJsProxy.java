/*
 * The MIT License
 *
 * Copyright 2021 michael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A proxy for the {@link EventSets} class, exposing its functionality at the 
 * JavaScript level.
 * 
 * @author michael
 */
public class EventSetsJsProxy {
   
    public final EventSet all = EventSets.all;
    
    public final EventSet none = EventSets.none;
    
    public static EventSet not( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return ifce.negate();
	}
	    
	public static EventSet anyOf( final EventSet... ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new ComposableEventSet.AnyOf( new HashSet<>(Arrays.asList(ifces)) );
	}
	    
    public static EventSet allOf( final EventSet... ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new ComposableEventSet.AllOf(new HashSet<>(Arrays.asList(ifces)));
	}
    
}

package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;


/**
 * A <em>mathematical</em> set of events - a predicate for testing whether an
 * event is a member of a set or not. This allows for symbolic sets, such as
 * "all events" or "events of class X".
 * 
 * A subset of this class that has an internal list of events (a set in its
 * Collection Framework semantics), is {@link ExplicitEventSet}.
 * 
 * @author michael
 */
public interface EventSet extends java.io.Serializable {
	/**
	 * Implementation of the set membership function.
	 * 
	 * @param event  A candidate object to be tested for matching the criteria of  the set.
	 * @return {@code true} if the object is a member of this set.
	 */
	boolean contains(BEvent event);

}

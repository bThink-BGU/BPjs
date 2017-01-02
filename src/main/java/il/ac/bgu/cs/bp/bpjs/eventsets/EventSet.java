package il.ac.bgu.cs.bp.bpjs.eventsets;


/**
 * A <em>mathematical</em> set of events. I particular - the word "set" here is not
 * used in its {@code java.util.collection} semantics, to allow symbolic sets, such as
 * "all events" or "events of class X".
 * 
 * @author michael
 */
public interface EventSet extends java.io.Serializable {
	/**
	 * Implementation of the set membership function.
	 * 
	 * @param o  A candidate object to be tested for matching the criteria of  the set.
	 * @return true if the object matches the criteria of the set.
	 */
	boolean contains(Object o);

}

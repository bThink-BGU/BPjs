package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.ScriptableObject;

/**
 * A base class for events. Each event has a name and optional data, which is a
 * Javascript object.
 *
 * For two events to be equal, their names and data have to match.
 *
 * Each event implicitly defines a singleton {@link EventSet}, which contains
 * only itself.
 */
@SuppressWarnings("serial")
public class BEvent implements Comparable<BEvent>, EventSet, java.io.Serializable {

    private static final AtomicInteger INSTANCE_ID_GEN = new AtomicInteger(0);

    /**
     * Name of the event. Public access, so that the Javascript code feels
     * natural.
     */
    public final String name;

    /**
     * Extra data for the event. Public access, so that the Javascript code
     * feels natural.
     */
    public final Object maybeData;

    public static BEvent named(String aName) {
        return new BEvent(aName);
    }

    public BEvent(String aName) {
        this(aName, null);
    }

    public BEvent(String aName, Object someData) {
        name = aName;
        maybeData = someData;
    }

    public BEvent() {
        this("BEvent-" + INSTANCE_ID_GEN.incrementAndGet());
    }

    @Override
    public String toString() {
        return "[BEvent name:" + name + (getDataField().map(v -> " data:" + v).orElse("")) + "]";
    }

    public String getName() {
        return name;
    }

    /**
     * @return The data field of the event.
     */
    public Optional<Object> getDataField() {
        return Optional.ofNullable(maybeData);
    }
    
    /**
     * A Javascript accessor for the event's data. If you are using this method 
     * from Java code, you may want to consider using {@link #getDataField()}.
     * 
     * @return the event's data, or {@code null}.
     * @see #getDataField() 
     */
    public Object getData() {
        return maybeData;
    }

    @Override
    public boolean equals(Object obj) {
        // Circuit breakers
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BEvent)) {
            return false;
        }

        BEvent other = (BEvent) obj;
        // simple cases
        if (!name.equals(other.name)) {
            return false;
        }
        
        if ( (maybeData!=null) ^ (other.getDataField().isPresent()) ) {
            // one has data, the other does not.
            return false;
        }

        if ( (maybeData!=null) ) { // and, by above test, other also has data
            // OK, delve into Javascript semantics.
            Object theirData = other.getDataField().get();
            if (!(maybeData.getClass().isAssignableFrom(theirData.getClass())
                    || theirData.getClass().isAssignableFrom(maybeData.getClass()))) {
                return false; // not same type of data.
            }

            // Evaluate datas.
            return jsObjectsEqual(maybeData, theirData);

        } else {
            // whew - both don't have data
            return true;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(BEvent e) {
        return name.compareTo(e.getName());
    }

    @Override
    public boolean contains(BEvent event) {
        return equals(event);
    }

    /**
     * Deep-compare of {@code o1} and {@code o2}. Recurses down these objects,
     * when needed.
     *
     * <em>DOES NOT DEAL WITH CIRCULAR REFERENCES!</em>
     *
     * @param o1
     * @param o2
     * @return {@code true} iff both objects are recursively equal
     */
    private boolean jsObjectsEqual(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null ^ o2 == null) {
            return false;
        }

        // Concatenated strings in Rhino have a different type. We need to manually
        // resolve to String semantics, which is what the following lines do.
        if (o1 instanceof ConsString) {
            o1 = o1.toString();
        }
        if (o2 instanceof ConsString) {
            o2 = o2.toString();
        }

        if (!o1.getClass().equals(o2.getClass())) {
            return false;
        }

        // established: o1 and o2 are non-null and of the same class.
        return (o1 instanceof ScriptableObject)
                ? jsScriptableObjectEqual((ScriptableObject) o1, (ScriptableObject) o2)
                : o1.equals(o2);
    }

    private boolean jsScriptableObjectEqual(ScriptableObject o1, ScriptableObject o2) {
        Object[] o1Ids = o1.getIds();
        Object[] o2Ids = o2.getIds();
        if (o1Ids.length != o2Ids.length) {
            return false;
        }
        return Stream.of(o1Ids).allMatch(id -> jsObjectsEqual(o1.get(id), o2.get(id)))
                && Stream.of(o2Ids).allMatch(id -> jsObjectsEqual(o1.get(id), o2.get(id)));
    }
  
}

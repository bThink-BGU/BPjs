package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A base class for events. Each event has a name and optional data, which is a
 * JavaScript object.
 *
 * For two events to be equal, their names and data have to match.
 *
 * Each event implicitly defines a singleton {@link EventSet}, which contains
 * only itself.
 */
@SuppressWarnings("serial")
public class BEvent implements Comparable<BEvent>, EventSet, java.io.Serializable {

    private static final ConcurrentHashMap<String,AtomicInteger> NAME_INDICES =new ConcurrentHashMap<>();

    /**
     * Name of the event. Public access, so that the JavaScript code feels
     * natural.
     */
    public final String name;

    /**
     * Extra data for the event. Public access, so that the JavaScript code
     * feels natural.
     */
    public Object maybeData;

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
        String className[] = getClass().getCanonicalName().split("\\.");
        String baseName = className[className.length-1];
        int index = NAME_INDICES.computeIfAbsent(baseName, n->new AtomicInteger()).incrementAndGet();
        name = baseName+"-"+index;
        maybeData = null;
    }

    @Override
    public String toString() {
        return "[BEvent name:" + name + (getDataField().map(v -> " data:" + dataToString(v)).orElse("")) + "]";
    }

    public String getName() {
        return name;
    }

    /**
     * @return The data field of the event.
     */
    private Optional<Object> getDataField() {
        return Optional.ofNullable(maybeData);
    }
    
    /**
     * A JavaScript accessor for the event's data, which may be @{code null}. 
     * If you are using this method from Java code, you may want to consider 
     * using {@link #getDataField()}, for a more Java-friendly API.
     * 
     * @return the event's data, or {@code null}.
     * @see #getDataField()
     */
    public Object getData() {
        return maybeData;
    }

    public void setData(Object maybeData) {
        this.maybeData = maybeData;
    }

    /**
     * Take the data field and give it some sensible string representation.
     * @param data
     * @return String representation of {@code data}.
     */
    private String dataToString( Object data ) {
        return ScriptableUtils.stringify(data);
    }
    
    @Override
    public boolean equals(Object obj) {
        // Simple global cases
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof BEvent)) return false;
        
        BEvent other = (BEvent) obj;
        
        /// Simple BEvent cases
        if (!name.equals(other.name))  return false;
        // same (possibly null) data on both.
        if ( maybeData == other.getData() ) return true;
        // one has data, the other does not.
        if ( (maybeData==null) ^ (other.getDataField().isEmpty()) ) return false;
        
        // Not so simple data equality casses. Let's hope they have state-based equals() implemented :^)
        Object theirData = other.getDataField().get();
        return ScriptableUtils.jsEquals(maybeData, theirData);

    }

    @Override
    public int hashCode() {
        return 19*Objects.hashCode(name) ^ getDataField().map(ScriptableUtils::jsHashCode).orElse(0);
    }

    @Override
    public int compareTo(BEvent e) {
        return name.compareTo(e.getName());
    }

    @Override
    public boolean contains(BEvent event) {
        return equals(event);
    }

}

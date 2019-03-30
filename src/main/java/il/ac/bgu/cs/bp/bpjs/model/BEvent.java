package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.mozilla.javascript.Scriptable;

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
    public Optional<Object> getDataField() {
        return Optional.ofNullable(maybeData);
    }
    
    /**
     * A JavaScript accessor for the event's data. If you are using this method 
     * from Java code, you may want to consider using {@link #getDataField()}.
     * 
     * @return the event's data, or {@code null}.
     * @see #getDataField() 
     */
    public Object getData() {
        return maybeData;
    }
    
    /**
     * Take the data field and give it some sensible string representation.
     * @param data
     * @return String representation of {@code data}.
     */
    private String dataToString( Object data ) {
        if ( data == null ) return "<null>";
        return ( data instanceof Scriptable ) ? ScriptableUtils.toString((Scriptable) data)
                                              : Objects.toString(data);
    }
    
    @Override
    public boolean equals(Object obj) {
        // Circuit breakers
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof BEvent)) return false;
        
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
            return ScriptableUtils.jsEquals(maybeData, theirData);

        } else {
            // whew - both don't have data
            return true;
        }
    }

    @Override
    public int hashCode() {
        return 19*name.hashCode() ^ getDataField().map(ScriptableUtils::jsHash).orElse(0);
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

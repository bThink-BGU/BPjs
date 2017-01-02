package il.ac.bgu.cs.bp.bpjs.eventsets;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

@SuppressWarnings("serial")
public class HashEventSet extends HashSet<EventSet> implements EventSet, Serializable {

    private String name = null;

    public HashEventSet(EventSet... eSetInterfaces) {
        super();

        for (EventSet eSetInterface : eSetInterfaces) {
            add(eSetInterface);
        }
    }

    public HashEventSet(String name, EventSet... eSetInterfaces) {
        this(eSetInterfaces);
        this.name = name;
    }

    @Override
    public boolean contains(Object o) {
        Iterator<EventSet> itr = this.iterator();

        while (itr.hasNext()) {
            EventSet eSetInterface = itr.next();
            if (eSetInterface.contains(o)) {
                return true;
            }
        }

        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return super.toString();
        }
    }

}
package il.ac.bgu.cs.bp.bpjs.diningphil;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;

public class NodeStub extends Node {
	String name;

	static BEvent X = new BEvent("X");
	static BEvent Y = new BEvent("Y");

	public NodeStub(String name) {
		super();
		this.name = name;
	}

	@Override
	public String toString() {
		return "[" + name + "]";
	}

	@Override
	public Set<BEvent> getPossibleEvents() {
		switch (name) {
		case "B,B":
			return new HashSet<BEvent>(asList(X, Y));
		case "B,A":
			return new HashSet<BEvent>(asList(Y));
		case "A,B":
			return new HashSet<BEvent>(asList(X));
		case "A,A":
			return new HashSet<BEvent>(asList(X));
		}
		return new HashSet<BEvent>();
	}

	@Override
	public Node getNextNode(BEvent e) {
		switch (name) {
		case "B,B":
			if (e == X)
				return new NodeStub("B,A");
			if (e == Y)
				return new NodeStub("A,B");
		case "B,A":
			if (e == X)
				return new NodeStub("A,A");
		case "A,B":
			if (e == Y)
				return new NodeStub("A,A");
		case "A,A":
			if (e == X)
				return new NodeStub("A,B");

		}
		return new Node();
	}

	@Override
	public boolean check() {
		return name.equals("A,B") ? false : true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeStub other = (NodeStub) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
//package il.ac.bgu.cs.bp.bpjs.diningphil;
//
//import static java.util.Arrays.asList;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import il.ac.bgu.cs.bp.bpjs.events.BEvent;
//
//public class NodeStub2 extends Node {
//	String name;
//
//	static BEvent X = new BEvent("X");
//	static BEvent Y = new BEvent("Y");
//
//	public NodeStub2(String name) {
//		super(null,null); // compilation workaround
//		this.name = name;
//	}
//
//	@Override
//	public String toString() {
//		return "[" + name + "]";
//	}
//
//	@Override
//	public Set<BEvent> getPossibleEvents() {
//		switch (name) {
//		case "B,B":
//			return new HashSet<BEvent>(asList(X, Y));
//		case "B,A":
//			return new HashSet<BEvent>(asList(Y));
//		case "A,B":
//			return new HashSet<BEvent>(asList(X));
//		case "A,A":
//			return new HashSet<BEvent>(asList(X));
//		}
//		return new HashSet<BEvent>();
//	}
//
//	@Override
//	public Node getNextNode(BEvent e) {
//		switch (name) {
//		case "B,B":
//			if (e == X)
//				return new NodeStub2("B,A");
//			if (e == Y)
//				return new NodeStub2("A,A");
//		case "B,A":
//			if (e == Y)
//				return new NodeStub2("A,A");
//		case "A,B":
//			if (e == X)
//				return new NodeStub2("A,A");
//			if (e == Y)
//				return new NodeStub2("B,A");
//		case "A,A":
//			if (e == X)
//				return new NodeStub2("A,B");
//			if (e == Y)
//				return new NodeStub2("B,A");
//
//		}
//		return null; // making this compile
////		return new Node();
//	}
//
//	@Override
//	public boolean check() {
//		return name.equals("B,A") ? false : true;
//	}
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((name == null) ? 0 : name.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		NodeStub2 other = (NodeStub2) obj;
//		if (name == null) {
//			if (other.name != null)
//				return false;
//		} else if (!name.equals(other.name))
//			return false;
//		return true;
//	}
//
//}
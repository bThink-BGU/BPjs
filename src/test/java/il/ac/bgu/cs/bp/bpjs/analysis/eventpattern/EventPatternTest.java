package il.ac.bgu.cs.bp.bpjs.analysis.eventpattern;


import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import static il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets.all;
import static org.junit.Assert.*;

public class EventPatternTest {
	
	EventPattern sut;
	
	@Before
	public void setUp() throws Exception {
		sut = new EventPattern();
	}
	
	@Test
	public void cornerCases() {
		List<BEvent> eventList = new ArrayList<>();
		
		assertTrue( sut.matches(eventList) );
		
		eventList.add( makeEvent("bbb") );
		assertFalse( sut.matches(eventList) );
		
	}
	
	@Test
	public void simpleTest() {
		List<? extends BEvent> eventList = ne("a","b","c");
		
		sut.append( makeEvent("a") ).append( makeEvent("b") );
		assertFalse( sut.matches( eventList ) );
		
		sut.append( makeEvent("c") );
		
		
		assertTrue( sut.matches( eventList ) );
		
		sut.append( makeEvent("c") );
		assertFalse( sut.matches( eventList ) );
	}
	
	@Test
	public void singleWildcardTest() {
		sut.append( makeEvent("a") );
		sut.append( all );
		sut.append( makeEvent("c" ));
		
		assertTrue( sut.matches( ne("a","b","c") ) );
		assertTrue( sut.matches( ne("a","fkasdjfhlaksdjfhlakj","c") ) );
		assertFalse( sut.matches( ne("a","b","c","d") ) );
		assertFalse( sut.matches( ne("a","b","d") ) );
		assertFalse( sut.matches( ne("d","b","c") ) );
		assertFalse( sut.matches( ne("d","b","d") ) );
	}
	
	@Test
	public void emptyMultiCharTest() {
		
		sut.append( makeEvent("a") );
		sut.appendStar( all );
		sut.append( makeEvent("c") );
		assertTrue( sut.matches( ne("a","c") ) );
		
		sut.clear();
		sut.append( makeEvent("a") );
		sut.appendStar( all );
		sut.appendStar( all );
		sut.append( makeEvent("c") );
		assertTrue( sut.matches( ne("a","c") ) );
	}
	
	@Test
	public void multiCharTest() {
		
		sut.append( makeEvent("a") );
		sut.appendStar( all );
		sut.append( makeEvent("c") );
		assertTrue( sut.matches( ne("a","c") ) );
		assertTrue( sut.matches( ne("a","x","c") ) );
		assertTrue( sut.matches( ne("a","x","x","c") ) );
		assertTrue( sut.matches( ne("a","x","x","x","c") ) );
		assertTrue( sut.matches( ne("a","a","c") ) );
		assertTrue( sut.matches( ne("a","c","c") ) );
		assertFalse( sut.matches( ne("b") ) );
		assertFalse( sut.matches( ne("c","a") ) );
		assertFalse( sut.matches( ne("a","b","b","a") ) );
		assertFalse( sut.matches( ne("a","c","d") ) );
		assertFalse( sut.matches( ne("d","a","c") ) );
		
		
		sut.clear();
		sut.append( makeEvent("a") );
		sut.appendStar( all );
		sut.appendStar( all );
		sut.append( makeEvent("c") );
		assertTrue( sut.matches( ne("a","c") ) );
		assertTrue( sut.matches( ne("a","x","c") ) );
		assertTrue( sut.matches( ne("a","x","x","c") ) );
		assertTrue( sut.matches( ne("a","x","x","x","c") ) );
		assertTrue( sut.matches( ne("a","a","c") ) );
		assertTrue( sut.matches( ne("a","c","c") ) );
		assertTrue( sut.matches( ne("a","a","c","c") ) );
		
		sut.clear();
		sut.append( makeEvent("a") );
		sut.appendStar( all );
		sut.append( makeEvent("c") );
		sut.appendStar( all );
		assertTrue( sut.matches( ne("a","c") ) );
		assertTrue( sut.matches( ne("a","x","c") ) );
		assertTrue( sut.matches( ne("a","x","x","c") ) );
		assertTrue( sut.matches( ne("a","x","x","x","c") ) );
		assertTrue( sut.matches( ne("a","a","c") ) );
		assertTrue( sut.matches( ne("a","c","c") ) );
		assertTrue( sut.matches( ne("a","a","c","c") ) );
		assertTrue( sut.matches( ne("a","c","x","x","c") ) );
		assertTrue( sut.matches( ne("a","c","d") ) );
		assertFalse( sut.matches( ne() ) );
		assertFalse( sut.matches( ne("x","a","c","d") ) );
		
	}
	
	@Test
	public void emptyMultiCharAtEndsTest() {
		
		sut.appendStar( all );
		sut.append( makeEvent("a") );
		sut.append( makeEvent("c") );
		assertTrue( sut.matches( ne("a","c") ) );
		
		sut.clear();
		sut.append( makeEvent("a") );
		sut.append( makeEvent("c") );
		sut.appendStar( all );
		assertTrue( sut.matches( ne("a","c") ) );
		
		sut.clear();
		sut.appendStar( all );
		sut.append( makeEvent("a") );
		sut.append( makeEvent("c") );
		sut.appendStar( all );
		assertTrue( sut.matches( ne("a","c") ) );
		
		sut.clear();
		sut.appendStar( all );
		sut.appendStar( all );
		sut.append( makeEvent("a") );
		sut.appendStar( all );
		sut.appendStar( all );
		sut.append( makeEvent("c") );
		sut.appendStar( all );
		sut.appendStar( all );
		assertTrue( sut.matches( ne("a","c") ) );
	}
	
	private BEvent makeEvent(String name) {
		return new BEvent(name);
	}
	
	private List<? extends BEvent> ne( String... s ) {
		List<BEvent> list = new ArrayList<>(s.length);
		for ( String en : s ) {
			list.add( makeEvent(en) );
		}
		return list;
	}
}

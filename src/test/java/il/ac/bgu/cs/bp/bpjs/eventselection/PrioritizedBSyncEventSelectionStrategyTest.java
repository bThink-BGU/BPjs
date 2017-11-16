package il.ac.bgu.cs.bp.bpjs.eventselection;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Scriptable;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsCodeEvaluationException;

public class PrioritizedBSyncEventSelectionStrategyTest {
	
	private static final BEvent evt1 = new BEvent("evt1");
	private static final BEvent evt2 = new BEvent("evt2");
	private static final BEvent evt3 = new BEvent("evt3");
	private static final BEvent evt4 = new BEvent("evt4");
	
	@Test
	public void testSelectableEvents_noBlocking() throws InterruptedException {
		  BProgram prog = new BProgram("bad"){
	            @Override
	            protected void setupProgramScope(Scriptable scope) {
	                evaluate("bp.registerBThread('BThread1', function() {\n"
	                		+ "bsync({ request:[ bp.Event('evt1') ],5});\n"
	                		+ "});"
	                		+ "bp.registerBThread('BThread2', function() {\n"
	                		+ "bsync({ request:[ bp.Event('evt2') ],10});\n"
	                		+ "});"
	                		+ "bp.registerBThread('BThread3', function() {\n"
	                		+ "bsync({ request:[ bp.Event('evt3') ],10});\n"
	                		+ "});",
	                        "hardcoded");
	            }
	        };
	        
	        try { 
	            new BProgramRunner(prog).start();
	            fail("System should have thrown an error due to uncompilable Javascript code.");
	        } catch (BPjsCodeEvaluationException exp) { 
	        	//assertEquals( 2, exp.getDetails());
	        }          

	        PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy();
//	        Set<BSyncStatement> stmts = new HashSet<>();
//	        stmts.add(BSyncStatement.make().getBthread().getBSyncStatement());
//	        assertEquals(new HashSet<>(Arrays.asList(evt2, evt3)), sut.selectableEvents(stmts, Collections.emptyList()));
		
      
//		Set<BSyncStatement> stmts = new HashSet<>();
//		stmts.add(BSyncStatement.make().request(Arrays.asList(evt1, evt2, evt3, evt4)));
//		stmts.add(BSyncStatement.make().request(Arrays.asList(evt2, evt3, evt4)));
//		       
//		assertEquals( new HashSet<>(Arrays.asList(evt1, evt2)), sut.selectableEvents(stmts, Collections.emptyList()));

	}
	
	
	@Test
	public void testSelectableEvents_withBlocking() {

	}

	
	@Test
	public void testgetValue() {
		
	}
	
//	@Test
//	public void testGetSeed() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSelect() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetRequestedAndNotBlocked() {
//		fail("Not yet implemented");
//	}

}

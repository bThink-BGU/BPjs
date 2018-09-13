/*
 * The MIT License
 *
 * Copyright 2018 michael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class PausingEventSelectionStrategyDecoratorTest {
    
    public PausingEventSelectionStrategyDecoratorTest() {
    }
    
    /**
     * Test of select method, of class PausingEventSelectionStrategyDecorator.
     */
    @Test
    public void testRateLimiting() {
        try {
            long testStart = System.currentTimeMillis();
            
            BProgramRunner runner = new BProgramRunner(new SingleResourceBProgram("HotNCold.js"));
            runner.addListener( new PrintBProgramRunnerListener() );
            
            PausingEventSelectionStrategyDecorator sut =
                new PausingEventSelectionStrategyDecorator(new SimpleEventSelectionStrategy());
            runner.getBProgram().setEventSelectionStrategy(sut);
            
            long pauseTime = 500;
            
            sut.setListener((caller) -> {
                try {
                    Thread.sleep(pauseTime);
                    caller.unpause();
                } catch (InterruptedException ex) {
                    Logger.getLogger(PausingEventSelectionStrategyDecoratorTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
            Thread workerThread = new Thread(runner);
            workerThread.start();
            workerThread.join();
            
            long testEnd =  System.currentTimeMillis();
            long expectedMinimum = 6 * pauseTime; // 6 is the number of events selected in HotNCold.js.
            assert( expectedMinimum <= (testEnd-testStart) );
            
        } catch (InterruptedException ex) {
            Logger.getLogger(PausingEventSelectionStrategyDecoratorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getListener method, of class PausingEventSelectionStrategyDecorator.
     */
    @Test
    public void testGetListener() {
        
        PausingEventSelectionStrategyDecorator instance = 
            new PausingEventSelectionStrategyDecorator( new SimpleEventSelectionStrategy());
        PausingEventSelectionStrategyDecorator.Listener l =  caller -> {
            System.out.println("L called");
        };
        PausingEventSelectionStrategyDecorator.Listener expResult = l;
        instance.setListener(l);
        PausingEventSelectionStrategyDecorator.Listener result = instance.getListener();
        assertEquals(expResult, result);
        
    }
    
}

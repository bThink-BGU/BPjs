/*
 * The MIT License
 *
 * Copyright 2021 michael.
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
package il.ac.bgu.cs.bp.bpjs;

import il.ac.bgu.cs.bp.bpjs.internal.BPjsRhinoContextFactory;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import java.util.function.Consumer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Central place for application-level, static BPjs resources.
 * 
 * @author michael
 */
public class BPjs {
    
    /**
     * Top-level scope for all BPjs code.
     */
    private static ScriptableObject BPJS_SCOPE;

    private static ExecutorServiceMaker executorServiceMaker = new ExecutorServiceMaker();
    
    private static boolean logDuringVerification = false;

    static {
        ContextFactory.initGlobal( new BPjsRhinoContextFactory()) ;
        makeBPjsScope();
    }
    
    /**
     * Initializes a Rhino context fit for BPjs execution. This means, for
     * example, setting the optimization level to -1, and the ES version.
     * 
     * <B>NOTE</B> Must call {@code Context.exit()} after calling this method. 
     * This can either be done using a try/catch block, or by using try-with-resources.
     * 
     * @return A Rhino context for BPjs executions.
     */
    public static Context enterRhinoContext() {
        return Context.enter();
    }
    
    /**
     * Returns a sealed, frozen scope that all BPjs code and evaluations run 
     * under.
     * 
     * @return The top-level scope for all BPjs code.
     */
    public static ScriptableObject getBPjsScope() {
        return BPJS_SCOPE;
    }
    
    /**
     * Convenience method for running code that requires Rhino context. Ensures
     * that the context if closed after the block executes.
     * @param block The block of code to be executed with the context.
     */
    public static void withContext( Consumer<Context> block ) {
        try (Context cx = BPjs.enterRhinoContext()) {
           block.accept(cx); 
        }
    }
    
    /**
     * Creates a new scope for running BPjs code. This scope's parent scope
     * is the JVM-global BPjs scope.
     * 
     * @return A new scope to run BPjs code in.
     * @see #getBPjsScope() 
     */
    public static Scriptable makeBPjsSubScope() {
        try (Context cx = enterRhinoContext()) {
            Scriptable retVal = cx.newObject(BPjs.getBPjsScope());
            retVal.setPrototype(BPjs.getBPjsScope());
            retVal.setParentScope(null);
            return retVal;
        }
    }
    
    private static void makeBPjsScope() {
        try (Context cx = enterRhinoContext()) {
            ImporterTopLevel importer = new ImporterTopLevel(cx);
            BPJS_SCOPE = cx.initStandardObjects(importer, true); // create and seal
            // NOTE: global extensions to BPjs scopes would go here, if we decide to create them.
        }
    }

    /**
     * Returns the maker of executor service used by BPjs to advance b-threads.
     * @return the current executor service maker.
     */
    public static ExecutorServiceMaker getExecutorServiceMaker() {
        return executorServiceMaker;
    }

    /**
     * Set the executor service maker BPjs uses to advance b-threads.
     * @param executorServiceMaker the new executor service maker to be used.
     */
    public static void setExecutorServiceMaker(ExecutorServiceMaker executorServiceMaker) {
        BPjs.executorServiceMaker = executorServiceMaker;
    }

    /**
     * When set to {@code false}, calls to {@code bp.log} during verifications will
     * be ignored. Defaults to {@code false}.
     * @return Should the system honor logging calls during verifications.
     */
    public static boolean isLogDuringVerification() {
        return logDuringVerification;
    }

    /**
     * Sets whether calls to the logger should be respected during verification.
     * @param logDuringVerification 
     */
    public static void setLogDuringVerification(boolean logDuringVerification) {
        BPjs.logDuringVerification = logDuringVerification;
    }
    
    public static String getPomVersion(){
        Package mainPackage = BPjs.class.getPackage();
        String version = mainPackage.getImplementationVersion();
        String groupId = mainPackage.getName();
        String artifactId = mainPackage.getImplementationTitle();

        return version;   
    
}
